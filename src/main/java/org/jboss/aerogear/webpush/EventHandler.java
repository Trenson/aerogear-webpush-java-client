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

import io.netty.handler.codec.http2.Http2Headers;

/**
 * Allows a WebPush client the ability to handle responses from the WebPush Server.
 */
interface EventHandler {

    /**
     * Fired before an outbound event occurs.
     *
     * @param headers the headers of the outbound request
     */
    void outbound(Http2Headers headers);

    /**
     * Fired after an inbound event occurs.
     *
     * @param headers the headers returned from the register request.
     * @param streamId the streamId for this response.
     */
    void inbound(Http2Headers headers, int streamId);

    /**
     * Fired after an inbound push promise event occurs.
     *
     * @param headers the headers returned from the register request.
     * @param streamId the streamId for this response.
     * @param promisedStreamId the promisedStreamId for this response.
     */
    void pushPromise(Http2Headers headers, int streamId, int promisedStreamId);

    /**
     * Notifications send from the WebPush server
     *
     * @param data the body of the application server PUT request.
     * @param streamId the streamId for this notification.
     */
    void notification(String data, int streamId);

    /**
     * Invoked when a general message should be displayed.
     *
     * @param message the message to be displayed
     */
    void message(String message);
}
