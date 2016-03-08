/*
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.webpush;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MetaData.Request;
import org.eclipse.jetty.http.MetaData.Response;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.Stream.Listener;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.util.Callback;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class WebPushClient {

    private static final HttpFields HTTP_FIELDS_WITH_PREFER_HEADER;

    static {
        HTTP_FIELDS_WITH_PREFER_HEADER = new HttpFields();
        HTTP_FIELDS_WITH_PREFER_HEADER.add("prefer", "wait=0");
    }

    private final ConcurrentMap<Subscription, Consumer<Optional<PushMessage>>> monitoredSubscriptions
            = new ConcurrentHashMap<>();

    private final JettyHttp2Client http2Client;

    public WebPushClient() {
        http2Client = new JettyHttp2Client("localhost", 8443, true);
    }

    public WebPushClient(final String webPushServerURI, final boolean trustAll) {
        Objects.requireNonNull(webPushServerURI, "webPushServerURI");
        final URI uri = URI.create(webPushServerURI);
        http2Client = new JettyHttp2Client(uri.getHost(), uri.getPort(), trustAll);
    }

    public WebPushClient(final String host, final int port, final boolean trustAll) {
        http2Client = new JettyHttp2Client(host, port, trustAll);
    }

    public void connect() throws Exception {
        http2Client.connect();
    }

    public void disconnect() throws Exception {
        http2Client.disconnect();
    }

    public void subscribe(final Consumer<Subscription> consumer) {
        Objects.requireNonNull(consumer, "subscriptionConsumer");
        http2Client.postRequest("/webpush/subscribe", new Listener.Adapter() {

            @Override
            public void onHeaders(final Stream stream, final HeadersFrame frame) {
                final HttpFields headers = frame.getMetaData().getFields();

                final String subscriptionResource = headers.get(HttpHeader.LOCATION);
                final List<String> links = headers.getValuesList("Link");
                final String pushResource = ParseUtils.parseLink(links, "urn:ietf:params:push");
                final String receiptSubscribeResource = ParseUtils.parseLink(links, "urn:ietf:params:push:receipt");
                final LocalDateTime createdDateTime = LocalDateTime.now();  //FIXME parse header
                final String cacheControl = headers.get(HttpHeader.CACHE_CONTROL);
                final Long expirationTime = ParseUtils.parseMaxAge(cacheControl);

                final Subscription subscription = new Subscription.Builder(subscriptionResource)
                        .setPushResource(pushResource)
                        .setReceiptSubscribeResource(receiptSubscribeResource)
                        .setCreatedDateTime(createdDateTime).setExpirationTime(expirationTime)
                        .createSubscription();
                consumer.accept(subscription);
            }
        });
    }

    public void deleteSubscription(final Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription");
        cancelMonitoring(subscription);
        http2Client.deleteRequest(subscription.subscriptionResource(), new Listener.Adapter());
    }

    public void monitor(final Subscription subscription, final Consumer<Optional<PushMessage>> consumer) {
        monitor(subscription, false, consumer);
    }

    public void monitor(final Subscription subscription,
                        final boolean nowait,
                        final Consumer<Optional<PushMessage>> consumer) {
        Objects.requireNonNull(subscription, "subscription");
        Objects.requireNonNull(consumer, "pushMessageConsumer");
        final Consumer<Optional<PushMessage>> prevConsumer = monitoredSubscriptions.putIfAbsent(subscription, consumer);
        if (prevConsumer != null) {
            return; //this subscription has already monitored
        }
        http2Client.getRequest(subscription.subscriptionResource(), new Listener.Adapter() {

            private PushMessage.Builder builder;

            @Override
            public Listener onPush(Stream stream, PushPromiseFrame frame) {
                if (builder != null) {
                    throw new IllegalStateException(
                            "PushMessage.Builder must not be initialized before PUSH_PROMISE frame");
                }
                final Request request = (Request) frame.getMetaData();
                final String pushMessagePath = request.getURI().getPath();
                builder = new PushMessage.Builder(pushMessagePath);
                return this;
            }

            @Override
            public void onHeaders(Stream stream, HeadersFrame frame) {
                final Response response = (Response) frame.getMetaData();
                if (response.getStatus() == 204) {
                    consumer.accept(Optional.empty());
                    return;
                }
                if (builder == null) {
                    throw new IllegalStateException("PushMessage.Builder must be initialized before HEADERS frame");
                }
                builder.receivedDateTime(LocalDateTime.now())   //TODO parse "date" header
                        .createdDateTime(null);   //TODO parse "last-modified" header
            }

            @Override
            public void onData(Stream stream, DataFrame frame, Callback callback) {
                if (builder == null) {
                    throw new IllegalStateException("PushMessage.Builder must be initialized before DATA frame");
                }
                //TODO optimize data read
                final ByteBuffer dataBuffer = frame.getData();
                final CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer);
                builder.addDataFrame(charBuffer.toString());
                callback.succeeded();
                if (frame.isEndStream()) {
                    Optional<PushMessage> pushMessage = Optional.of(builder.build());
                    builder = null;
                    acknowledge(pushMessage.get());
                    consumer.accept(pushMessage);
                }
            }
        }, nowait ? HTTP_FIELDS_WITH_PREFER_HEADER : null);
    }

    private void acknowledge(final PushMessage pushMessage) {
        Objects.requireNonNull(pushMessage, "pushMessage");
        http2Client.deleteRequest(pushMessage.resource(), new Listener.Adapter());
    }

    public void cancelMonitoring(final Subscription subscription) {
        final Consumer<Optional<PushMessage>> consumer = monitoredSubscriptions.remove(subscription);
        if (consumer == null) {
            return; //this subscription is not monitored
        }
        //TODO implement cancel monitoring when WebPush Server will support this function
    }
}
