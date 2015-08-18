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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2OrHttpChooser.SelectedProtocol;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;

import javax.net.ssl.SSLException;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;

final class NettyHttpClient {

    private static final AsciiString SCHEME = AsciiString.of("https");
    private static final AsciiString PUSH_RECEIPT_HEADER = AsciiString.of("push-receipt");
    private static final AsciiString TTL_HEADER = AsciiString.of("ttl");
    private static final AsciiString PREFER_HEADER = AsciiString.of("prefer");
    private static final AsciiString PREFER_HEADER_VALUE = AsciiString.of("wait=0");

    private final String host;
    private final int port;
    private final AsciiString authority;
    private final String pathPrefix;
    private final EventHandler handler;

    private NioEventLoopGroup workerGroup;
    private Channel channel;

    private NettyHttpClient(final Builder builder) {
        host = builder.host;
        port = builder.port;
        authority = AsciiString.of(host + ":" + port);
        pathPrefix = builder.pathPrefix;
        handler = builder.handler;
    }

    String host() {
        return host;
    }

    int port() {
        return port;
    }

    String pathPrefix() {
        return pathPrefix;
    }

    void connect() throws Exception {
        workerGroup = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(host, port);
            b.handler(new NettyHttpClientInitializer(configureSsl(), host, port, handler));
            channel = b.connect().syncUninterruptibly().channel();
            System.out.println("Connected to [" + host + ':' + port + "][channelId=" + channel.id() + ']');
        } catch (final Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }

    private SslContext configureSsl() throws SSLException {
        return SslContextBuilder.forClient()
                                .sslProvider(SslProvider.JDK)
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                                .applicationProtocolConfig(new ApplicationProtocolConfig(
                                        Protocol.ALPN,
                                        SelectorFailureBehavior.FATAL_ALERT,
                                        SelectedListenerFailureBehavior.FATAL_ALERT,
                                        SelectedProtocol.HTTP_2.protocolName(),
                                        SelectedProtocol.HTTP_1_1.protocolName()))
                                .build();
    }

    void createSubscription(final String subscribeUrl) throws Exception {
        writeRequest(POST, subscribeUrl);
    }

    void deleteSubscription(final String endpointUrl) throws Exception {
        writeRequest(DELETE, endpointUrl);
    }

    void monitor(final String monitorUrl, final boolean now) throws Exception {
        final Http2Headers headers = http2Headers(GET, monitorUrl);
        if (now) {
            headers.add(PREFER_HEADER, PREFER_HEADER_VALUE);
        }
        writeRequest(headers);
    }

    void ack(final String messageUrl) throws Exception {
        writeRequest(DELETE, messageUrl);
    }

    private void writeRequest(final HttpMethod method, final String url) throws Exception {
        final Http2Headers headers = http2Headers(method, url);
        writeRequest(headers);
    }

    private void writeRequest(final Http2Headers headers) throws Exception {
        //TODO log request
        ChannelFuture requestFuture = channel.writeAndFlush(headers).sync();
        requestFuture.sync();
    }

    private Http2Headers http2Headers(final HttpMethod method, final String url) {
        return new DefaultHttp2Headers(false)
                .method(AsciiString.of(method.name()))
                .path(AsciiString.of(pathPrefix + url))
                .authority(authority)
                .scheme(SCHEME);
    }

    void disconnect() {
        if (channel != null) {
            channel.close();
            workerGroup.shutdownGracefully();
        }
    }

    boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    static class Builder {

        private String host = "localhost";
        private int port = 8443;
        private String pathPrefix = "/webpush";
        private EventHandler handler;

        public Builder(final EventHandler handler) {
            Objects.requireNonNull(handler, "handler");
            this.handler = handler;
        }

        public Builder host(final String host) {
            if (host != null) {
                this.host = host;
            }
            return this;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public Builder pathPrefix(final String pathPrefix) {
            if (pathPrefix != null) {
                this.pathPrefix = pathPrefix;
            }
            return this;
        }

        public Builder notificationHandler(final EventHandler handler) {
            this.handler = handler;
            return this;
        }

        public NettyHttpClient build() {
            return new NettyHttpClient(this);
        }
    }
}
