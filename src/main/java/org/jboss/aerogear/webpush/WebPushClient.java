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

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class WebPushClient {

    private final ConcurrentMap<Subscription, Consumer<Optional<PushMessage>>> monitoredSubscriptions
            = new ConcurrentHashMap<>();

    private final JettyHttp2Client http2Client;

    public WebPushClient() {
        http2Client = new JettyHttp2Client("localhost", 8443, "/webpush", true);
    }

    public WebPushClient(final String webPushServerURI, final boolean trustAll) {
        Objects.requireNonNull(webPushServerURI, "webPushServerURI");
        final URI uri = URI.create(webPushServerURI);
        http2Client = new JettyHttp2Client(uri.getHost(), uri.getPort(), uri.getPath(), trustAll);
    }

    public WebPushClient(final String host, final int port, final String pathPrefix, final boolean trustAll) {
        http2Client = new JettyHttp2Client(host, port, pathPrefix, trustAll);
    }

    public void connect() throws Exception {
        http2Client.connect();
    }

    public void disconnect() throws Exception {
        http2Client.disconnect();
    }

    public void subscribe(final Consumer<Subscription> consumer) {
        Objects.requireNonNull(consumer, "subscriptionConsumer");
        //TODO implement subscription
    }

    public void deleteSubscription(final Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription");
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
        //TODO implement monitor
    }

    public void cancelMonitoring(final Subscription subscription) {
        final Consumer<Optional<PushMessage>> consumer = monitoredSubscriptions.remove(subscription);
        if (consumer == null) {
            return; //this subscription is not monitored
        }
        //TODO implement cancel monitoring
    }
}
