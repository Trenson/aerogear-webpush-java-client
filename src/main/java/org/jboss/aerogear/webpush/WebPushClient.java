/**
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

import java.util.Objects;
import java.util.function.Consumer;

public class WebPushClient {

    private Subscription subscription;
    private Consumer<Subscription> subscriptionConsumer;
    private Consumer<PushMessage> pushMessageConsumer;

    public WebPushClient(final String webPushServerURL) {
        Objects.requireNonNull(webPushServerURL, "webPushServerURL");
    }

    public WebPushClient(final String host, final int port, final String path) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(path, "path");
    }

    public void subscribe(final Consumer<Subscription> consumer) {
        Objects.requireNonNull(consumer, "subscriptionConsumer");
        this.subscriptionConsumer = consumer;
        //TODO implement subscription
    }

    public void deleteSubscription() {
        if (subscription == null) {
            throw new IllegalStateException("not subscribed");
        }
        //TODO implement delete subscription
        subscription = null;
        subscriptionConsumer = null;
    }

    public void monitor(final Consumer<PushMessage> consumer) {
        monitor(consumer, false);
    }

    public void monitor(final Consumer<PushMessage> consumer, final boolean nowait) {
        Objects.requireNonNull(consumer, "pushMessageConsumer");
        this.pushMessageConsumer = consumer;
        //TODO implement monitor
    }

    public void cancelMonitoring() {
        if (pushMessageConsumer == null) {
            throw new IllegalStateException("monitoring disabled");
        }
        //TODO implement cancel monitoring
        pushMessageConsumer = null;
    }
}
