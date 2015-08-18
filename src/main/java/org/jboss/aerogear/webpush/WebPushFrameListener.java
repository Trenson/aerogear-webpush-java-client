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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.CharsetUtil;

import java.util.Objects;

class WebPushFrameListener implements Http2FrameListener {

    private final EventHandler callback;

    public WebPushFrameListener(final EventHandler callback) {
        this.callback = Objects.requireNonNull(callback, "callback");
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data,
                          int padding, boolean endOfStream) throws Http2Exception {
        callback.notification(data.toString(CharsetUtil.UTF_8), streamId);
        return 0;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                              int padding, boolean endOfStream) throws Http2Exception {
        callback.inbound(headers, streamId);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                              short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
        callback.inbound(headers, streamId);
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) throws Http2Exception {
        callback.pushPromise(headers, streamId, promisedStreamId);
    }

    @Override
    public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency,
                               short weight, boolean exclusive) throws Http2Exception {
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
    }

    @Override
    public void onSettingsAckRead(ChannelHandlerContext ctx) throws Http2Exception {
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
    }

    @Override
    public void onPingRead(ChannelHandlerContext ctx, ByteBuf data) throws Http2Exception {
    }

    @Override
    public void onPingAckRead(ChannelHandlerContext ctx, ByteBuf data) throws Http2Exception {
    }

    @Override
    public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId,
                             long errorCode, ByteBuf debugData) throws Http2Exception {
    }

    @Override
    public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId,
                                   int windowSizeIncrement) throws Http2Exception {
    }

    @Override
    public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType,
                               int streamId, Http2Flags flags, ByteBuf payload) {
    }
}
