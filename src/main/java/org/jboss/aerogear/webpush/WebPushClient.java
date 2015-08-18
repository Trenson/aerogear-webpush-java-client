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

import javax.net.ssl.SSLException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class WebPushClient {

    private final ConcurrentMap<Subscription, Consumer<Optional<PushMessage>>> monitoredSubscriptions
            = new ConcurrentHashMap<>();

    private final NettyHttpClient httpClient;

    public WebPushClient() {
        httpClient = new NettyHttpClient.Builder(new CallbackHandler())
                .build();
    }

    public WebPushClient(final String webPushServerURI) {
        Objects.requireNonNull(webPushServerURI, "webPushServerURI");
        final URI uri = URI.create(webPushServerURI);
        httpClient = new NettyHttpClient.Builder(new CallbackHandler())
                .host(uri.getHost())
                .port(uri.getPort())
                .pathPrefix(uri.getPath())
                .build();
    }

    public WebPushClient(final String host, final int port, final String pathPrefix) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(pathPrefix, "pathPrefix");
        httpClient = new NettyHttpClient.Builder(new CallbackHandler())
                .host(host)
                .port(port)
                .pathPrefix(pathPrefix)
                .build();
    }

    public void subscribe(final Consumer<Subscription> consumer) {
        Objects.requireNonNull(consumer, "subscriptionConsumer");
        connect();
        //TODO implement subscription
    }

    public void deleteSubscription(final Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription");
        connect();
        cancelMonitoring(subscription);
        //TODO implement delete subscription
    }

    public void monitor(final Subscription subscription, final Consumer<Optional<PushMessage>> consumer) {
        monitor(subscription, consumer, false);
    }

    public void monitor(final Subscription subscription,
                        final Consumer<Optional<PushMessage>> consumer,
                        final boolean nowait) {
        Objects.requireNonNull(subscription, "subscription");
        Objects.requireNonNull(consumer, "pushMessageConsumer");
        final Consumer<Optional<PushMessage>> prevConsumer = monitoredSubscriptions.putIfAbsent(subscription, consumer);
        if (prevConsumer != null) {
            return; //this subscription has already monitored
        }
        connect();
        //TODO implement monitor
    }

    public void cancelMonitoring(final Subscription subscription) {
        final Consumer<Optional<PushMessage>> consumer = monitoredSubscriptions.remove(subscription);
        if (consumer == null) {
            return; //this subscription is not monitored
        }
        connect();
        //TODO implement cancel monitoring
    }

    private void connect() {
        if (httpClient.isConnected()) {
            return;
        }
        synchronized (this) {
            if (httpClient.isConnected()) {
                return;
            }
            try {
                httpClient.connect();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (!httpClient.isConnected()) {
            return;
        }
        synchronized (this) {
            if (!httpClient.isConnected()) {
                return;
            }
            httpClient.disconnect();
        }
    }
}
