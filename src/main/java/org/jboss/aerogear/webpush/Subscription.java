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

/**
 * Object which holds subscription information:
 * <ul>
 *     <li>subscription resource, see {@link #subscriptionResource()}</li>
 *     <li>push resource, see {@link #pushResource()}</li>
 *     <li>receipt subscribe resource, see {@link #receiptSubscribeResource()}</li>
 *     <li>created date-time, see {@link #createdDateTime()}</li>
 *     <li>expiration time, see {@link #expirationTime()}</li>
 * </ul>
 *
 * New subscription object will be created by {@link WebPushClient}.
 */
public final class Subscription implements Serializable {

    private static final long serialVersionUID = 1229410345785780151L;

    private final String subscriptionResource;
    private final String pushResource;
    private final String receiptSubscribeResource;
    private final LocalDateTime createdDateTime;
    private final Long expirationTime;  //FIXME may be primitive type is preferable

    private Subscription(final Builder builder) {
        subscriptionResource = Objects.requireNonNull(builder.subscriptionResource, "subscriptionResource");
        pushResource = Objects.requireNonNull(builder.pushResource, "pushResource");
        receiptSubscribeResource = Objects.requireNonNull(builder.receiptSubscribeResource, "receiptSubscribeResource");
        createdDateTime = builder.createdDateTime;
        expirationTime = builder.expirationTime;
    }

    /**
     * A subscription resource is used to receive messages from a subscription and to delete a subscription
     * (see <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-6">Section 6</a>
     *  of WebPush protocol specification).
     *
     * The {@code Location} header field is used to identify subscription resource.
     * It is private to the user agent and should not be shared with its application server.
     *
     * @return subscription resource URI on WebPush Server.
     */
    public String subscriptionResource() { //TODO consider to make it package-private
        return subscriptionResource;
    }

    /**
     * A push resource is used to send messages to a subscription
     * (see <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-5">Section 5</a>
     *  of WebPush protocol specification).
     *
     * A link relation of type "urn:ietf:params:push" is used to identity a receipt subscribe resource.
     * It is public and shared by the user agent with its application server.
     *
     * @return push resource URI on WebPush Server.
     */
    public String pushResource() {
        return pushResource;
    }

    /**
     * A receipt subscribe resource is used by an application server to create a receipt subscription
     * (see <a href="https://tools.ietf.org/html/draft-thomson-webpush-protocol-00#section-4">Section 4</a>
     *  of WebPush protocol specification).
     *
     * A link relation of type "urn:ietf:params:push:receipt" is used to identity a receipt subscribe resource.
     * It is public and shared by the user agent with its application server.
     *
     * @return receipt subscribe resource URI on WebPush Server.
     */
    public String receiptSubscribeResource() {
        return receiptSubscribeResource;
    }

    /**
     * Date-time when subscription resource was created on the WebPush Server.
     *
     * @return created date-time.
     */
    public LocalDateTime createdDateTime() {
        return createdDateTime;
    }

    /**
     * Expiration time for current subscription on WebPush Server.
     *
     * @return expiration time in milli seconds.
     */
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

    static class Builder {

        private final String subscriptionResource;
        private String pushResource;
        private String receiptSubscribeResource;
        private LocalDateTime createdDateTime;
        private Long expirationTime;

        Builder(final String subscriptionResource) {
            this.subscriptionResource = subscriptionResource;
        }

        Builder setPushResource(String pushResource) {
            this.pushResource = pushResource;
            return this;
        }

        Builder setReceiptSubscribeResource(String receiptSubscribeResource) {
            this.receiptSubscribeResource = receiptSubscribeResource;
            return this;
        }

        Builder setCreatedDateTime(LocalDateTime createdDateTime) {
            this.createdDateTime = createdDateTime;
            return this;
        }

        Builder setExpirationTime(Long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        Subscription createSubscription() {
            return new Subscription(this);
        }
    }
}
