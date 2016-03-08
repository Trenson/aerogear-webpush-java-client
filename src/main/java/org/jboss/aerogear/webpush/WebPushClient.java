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

/**
 * Asynchronous HTTP/2 client for
 * <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00">WebPush protocol</a>.
 *
 * <p>Example of WebPushClient usage:
 * <pre>{@code
 *     WebPushClient webPushClient = new WebPushClient("https://localhost:8443", true);
 *     try {
 *         webPushClient.connect(); //connect to the WebPush Server
 *         webPushClient.subscribe(subscription -> {    //create subscription
 *             webPushClient.monitor(subscription, true, pushMessage -> {   //monitor without waiting of new messages
 *                 if (pushMessage.isPresent()) {
 *                     System.out.println(pushMessage.get());   //handle push message
 *                 } else {    //possible only if nowait == true
 *                     System.out.println("204 No Content");    //there are no new messages on the WebPush Server
 *                 }
 *             });
 *             webPushClient.monitor(subscription, System.out::println);    //monitor all new push messages
 *         });
 *     } finally {
 *         webPushClient.disconnect();  //disconnect from the WebPush Server
 *     }
 * }</pre>
 */
public class WebPushClient {

    private static final HttpFields HTTP_FIELDS_WITH_PREFER_HEADER;

    static {
        HTTP_FIELDS_WITH_PREFER_HEADER = new HttpFields();
        HTTP_FIELDS_WITH_PREFER_HEADER.add("prefer", "wait=0");
    }

    private final ConcurrentMap<Subscription, Consumer<Optional<PushMessage>>> monitoredSubscriptions
            = new ConcurrentHashMap<>();

    private final JettyHttp2Client http2Client;

    /**
     * Creates WebPush client which will work with local WebPush Server on port 8443
     * and will blindly trust all SSL certificates.
     */
    public WebPushClient() {
        http2Client = new JettyHttp2Client("localhost", 8443, true);
    }

    /**
     * Creates WebPush client which will work with your WebPush Server.
     *
     * @param webPushServerURI URI for your WebPush Server, example: {@code https://localhost:8443}.
     * @param trustAll         whether to blindly trust all certificates.
     */
    public WebPushClient(final String webPushServerURI, final boolean trustAll) {
        Objects.requireNonNull(webPushServerURI, "webPushServerURI");
        final URI uri = URI.create(webPushServerURI);
        http2Client = new JettyHttp2Client(uri.getHost(), uri.getPort(), trustAll);
    }

    /**
     * Creates WebPush client which will work with your WebPush Server.
     *
     * @param host     host for your WebPush Server.
     * @param port     port for your WebPush Server.
     * @param trustAll whether to blindly trust all certificates.
     */
    public WebPushClient(final String host, final int port, final boolean trustAll) {
        http2Client = new JettyHttp2Client(host, port, trustAll);
    }

    /**
     * Opens connection to WebPush Server.
     * This method has to be invoked before any other methods of this class.
     *
     * @throws Exception if something goes wrong.
     */
    public void connect() throws Exception {
        http2Client.connect();
    }

    /**
     * Disconnects from the server.
     * This method has to be invoked manually to free connection resources.
     *
     * @throws Exception if something goes wrong.
     */
    public void disconnect() throws Exception {
        http2Client.disconnect();
    }

    /**
     * Creates new subscription on WebPush Server.
     *
     * This method implements
     * <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-3">Section 3:
     * Subscribing for Push Messages</a> of WebPush protocol specification.
     *
     * @param consumer will be invoked when a new subscription is created.
     */
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

    /**
     * Removes specified subscription from the WebPush Server, see
     * <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-7.3">Section 7.3:
     * Subscription Expiration</a> of WebPush protocol specification.
     *
     * This method also invoked {@link #cancelMonitoring(Subscription)} for the specified subscription.
     *
     * @param subscription which will be removed.
     */
    public void deleteSubscription(final Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription");
        cancelMonitoring(subscription);
        http2Client.deleteRequest(subscription.subscriptionResource(), new Listener.Adapter());
    }

    /**
     * Enables monitoring of new push messages.
     *
     * This method implements
     * <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-6">Section 6:
     * Receiving Push Messages</a> of WebPush protocol specification.
     *
     * @param subscription for monitoring.
     * @param consumer     will be invoked when a new push message is received.
     */
    public void monitor(final Subscription subscription, final Consumer<Optional<PushMessage>> consumer) {
        monitor(subscription, false, consumer);
    }

    /**
     * Enables monitoring of new push messages.
     * Allows to specify a {@code Prefer} header field with a "wait" parameter set to "0".
     *
     * This method implements
     * <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-6">Section 6:
     * Receiving Push Messages</a> of WebPush protocol specification.
     *
     * @param subscription for monitoring.
     * @param nowait       adds {@code Prefer} header with {@code wait=0}.
     * @param consumer     will be invoked when a new push message is received.
     */
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

    /**
     * Cancels monitoring for specified subscription.
     *
     * @param subscription for which monitoring should be canceled.
     */
    public void cancelMonitoring(final Subscription subscription) {
        final Consumer<Optional<PushMessage>> consumer = monitoredSubscriptions.remove(subscription);
        if (consumer == null) {
            return; //this subscription is not monitored
        }
        //TODO implement cancel monitoring when WebPush Server will support this function
    }
}
