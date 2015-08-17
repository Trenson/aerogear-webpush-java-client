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

public class WebPushClient {

    private String subscriptionResource;
    private Subscription subscription;
    private SubscriptionListener subscriptionListener;
    private PushMessageListener pushMessageListener;

    public WebPushClient(final String webPushServerURL) {
        Objects.requireNonNull(webPushServerURL, "webPushServerURL");
    }

    public WebPushClient(final String host, final int port, final String path) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(path, "path");
    }

    public void subscribe(final SubscriptionListener listener) {
        Objects.requireNonNull(listener, "subscriptionListener");
        this.subscriptionListener = listener;
        //TODO implement subscription
    }

    public void deleteSubscription() {
        if (subscriptionResource == null || subscription == null) {
            throw new IllegalStateException("not subscribed");
        }
        //TODO implement delete subscription
        subscriptionResource = null;
        subscription = null;
        subscriptionListener = null;
    }

    public void monitor(final PushMessageListener listener) {
        monitor(listener, false);
    }

    public void monitor(final PushMessageListener listener, final boolean nowait) {
        Objects.requireNonNull(listener, "pushMessageListener");
        this.pushMessageListener = listener;
        //TODO implement monitor
    }

    public void cancelMonitoring() {
        if (pushMessageListener == null) {
            throw new IllegalStateException("monitoring disabled");
        }
        //TODO implement cancel monitoring
        pushMessageListener = null;
    }
}
