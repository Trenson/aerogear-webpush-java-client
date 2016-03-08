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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subscription implements Serializable {

    private static final long serialVersionUID = 1229410345785780151L;

    private final String subscriptionResource;
    private final String pushResource;
    private final String receiptSubscribeResource;
    private final LocalDateTime createdDateTime;
    private final Long expirationTime;  //FIXME may be primitive type is preferable

    public Subscription(final Builder builder) {
        subscriptionResource = Objects.requireNonNull(builder.subscriptionResource, "subscriptionResource");
        pushResource = Objects.requireNonNull(builder.pushResource, "pushResource");
        receiptSubscribeResource = Objects.requireNonNull(builder.receiptSubscribeResource, "receiptSubscribeResource");
        createdDateTime = builder.createdDateTime;
        expirationTime = builder.expirationTime;
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
                "\n\tsubscriptionResource='" + subscriptionResource + "'," +
                "\n\tpushResource='" + pushResource + "'," +
                "\n\treceiptSubscribeResource='" + receiptSubscribeResource + "'," +
                "\n\tcreatedDateTime=" + createdDateTime + ',' +
                "\n\texpirationTime=" + expirationTime +
                "\n}";
    }

    public static class Builder {

        private final String subscriptionResource;
        private String pushResource;
        private String receiptSubscribeResource;
        private LocalDateTime createdDateTime;
        private Long expirationTime;

        public Builder(final String subscriptionResource) {
            this.subscriptionResource = subscriptionResource;
        }

        public Builder setPushResource(String pushResource) {
            this.pushResource = pushResource;
            return this;
        }

        public Builder setReceiptSubscribeResource(String receiptSubscribeResource) {
            this.receiptSubscribeResource = receiptSubscribeResource;
            return this;
        }

        public Builder setCreatedDateTime(LocalDateTime createdDateTime) {
            this.createdDateTime = createdDateTime;
            return this;
        }

        public Builder setExpirationTime(Long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Subscription createSubscription() {
            return new Subscription(this);
        }
    }
}
