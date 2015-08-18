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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

class NettyHttpClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final EventHandler callback;
    private final String host;
    private final int port;
    private WebPushToHttp2ConnectionHandler connectionHandler;
    private HttpResponseHandler responseHandler;
    private Http2SettingsHandler settingsHandler;

    public NettyHttpClientInitializer(SslContext sslCtx, final String host, final int port, final EventHandler callback) {
        this.sslCtx = sslCtx;
        this.callback = callback;
        this.host = host;
        this.port = port;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        final Http2Connection connection = new DefaultHttp2Connection(false);
        connectionHandler = new WebPushToHttp2ConnectionHandler(connection,
                new DefaultHttp2FrameReader(),
                new DefaultHttp2FrameWriter(),
                new DelegatingDecompressorFrameListener(
                        new DefaultHttp2Connection(false),
                        new WebPushFrameListener(callback)));
        responseHandler = new HttpResponseHandler(callback);
        settingsHandler = new Http2SettingsHandler(ch.newPromise());
        configureSsl(ch);
    }

    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     */
    private void configureSsl(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        final SslHandler sslHandler = sslCtx.newHandler(ch.alloc(), host, port);
        sslHandler.handshakeFuture().addListener(new SslHandshakeListener(callback));
        pipeline.addLast("SslHandler", sslHandler);
        pipeline.addLast("Http2Handler", connectionHandler);
        ch.pipeline().addLast("Logger", new UserEventLogger(callback));
        configureEndOfPipeline(pipeline);
    }

    protected void configureEndOfPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("Http2SettingsHandler", settingsHandler);
        pipeline.addLast("HttpResponseHandler", responseHandler);
    }

    /**
     * Class that logs any User Events triggered on this channel.
     */
    private static class UserEventLogger extends ChannelInboundHandlerAdapter {

        private final EventHandler handler;

        UserEventLogger(final EventHandler handler) {
           this.handler = handler;
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof SslHandshakeCompletionEvent) {
                SslHandshakeCompletionEvent sslEvent = (SslHandshakeCompletionEvent) evt;
                if (!sslEvent.isSuccess()) {
                    handler.message(sslEvent.cause().getMessage());
                }
            }
            ctx.fireUserEventTriggered(evt);
        }
    }

    public static class WebPushToHttp2ConnectionHandler extends Http2ConnectionHandler {

        public WebPushToHttp2ConnectionHandler(final Http2Connection connection,
                                               final Http2FrameReader frameReader,
                                               final Http2FrameWriter frameWriter,
                                               final Http2FrameListener listener) {
            super(connection, frameReader, frameWriter, listener);
        }

        @Override
        public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
            if (msg instanceof Http2Headers) {
                final Http2Headers message = (Http2Headers) msg;
                final Http2ConnectionEncoder encoder = encoder();
                final int streamId = connection().local().nextStreamId();
                encoder.writeHeaders(ctx, streamId, message, 0, true, promise);
            } else {
                ctx.write(msg, promise);
            }
        }

        @Override
        public void onException(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            super.onException(ctx, cause);
        }
    }

    private static class SslHandshakeListener implements GenericFutureListener<Future<Channel>> {

        private final EventHandler handler;

        private SslHandshakeListener(final EventHandler handler) {
            this.handler = handler;
        }

        @Override
        public void operationComplete(final Future<Channel> future) throws Exception {
            if (!future.isSuccess()) {
                handler.message(future.cause().getMessage());
            }
        }
    }
}
