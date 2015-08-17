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

import java.time.LocalDateTime;
import java.util.Objects;

public class Subscription {  //FIXME should it be serializable?

    private final String subscriptionResource;
    private final String pushResource;
    private final String receiptSubscribeResource;
    private final LocalDateTime createdDateTime;
    private final Long expirationTime;  //FIXME may be primitive type is preferable

    public Subscription(final String subscriptionResource,
                        final String pushResource,
                        final String receiptSubscribeResource,
                        final LocalDateTime createdDateTime,
                        final Long expirationTime) {
        Objects.requireNonNull(subscriptionResource, "subscriptionResource");
        Objects.requireNonNull(pushResource, "pushResource");
        Objects.requireNonNull(receiptSubscribeResource, "receiptSubscribeResource");
        this.subscriptionResource = subscriptionResource;
        this.pushResource = pushResource;
        this.receiptSubscribeResource = receiptSubscribeResource;
        this.createdDateTime = createdDateTime;
        this.expirationTime = expirationTime;
    }

    public String subscriptionResource() {
        return subscriptionResource;
    }

    public String pushResource() {
        return pushResource;
    }

    public String receiptSubscribeResource() {
        return receiptSubscribeResource;
    }

    public LocalDateTime createdDateTime() {
        return createdDateTime;
    }

    public long expirationTime() {
        return expirationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subscription)) {
            return false;
        }
        Subscription that = (Subscription) o;
        return subscriptionResource.equals(that.subscriptionResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionResource);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "subscriptionResource='" + subscriptionResource + '\'' +
                ", pushResource='" + pushResource + '\'' +
                ", receiptSubscribeResource='" + receiptSubscribeResource + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", expirationTime=" + expirationTime +
                '}';
    }
}
