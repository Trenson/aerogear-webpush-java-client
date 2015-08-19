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

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData.Request;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream.Listener;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

class JettyHttp2Client {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    private final String host;
    private final int port;
    private final String serverUri;

    private final HTTP2Client client;
    private final SslContextFactory sslContextFactory;

    private Session session;

    JettyHttp2Client(final String host, final int port, final boolean trustAll) {
        Objects.requireNonNull(host, "host");
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port value must be between 0 and 65535, current value: " + port);
        }
        this.host = host;
        this.port = port;
        this.serverUri = "https://" + host + ":" + port;

        client = new HTTP2Client();
        sslContextFactory = new SslContextFactory(trustAll);
        client.addBean(sslContextFactory);
    }

    public void connect() throws Exception {
        client.start();
        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(sslContextFactory, new InetSocketAddress(host, port), new ServerSessionListener.Adapter(),
                sessionPromise);
        session = sessionPromise.get(5, TimeUnit.SECONDS);
    }

    public void disconnect() throws Exception {
        client.stop();
        session = null;
    }

    public void getRequest(final String path, final Listener listener, final HttpFields httpFields) {
        sendRequest(GET, path, listener, httpFields);
    }

    public void postRequest(final String path, final Listener listener) {
        sendRequest(POST, path, listener, null);
    }

    public void deleteRequest(final String path, final Listener listener) {
        sendRequest(DELETE, path, listener, null);
    }

    private void sendRequest(final String method, final String path,
                             final Listener responseListener, final HttpFields httpFields) {
        //FIXME be sure that session created
        Request requestMetaData = new Request(method, new HttpURI(serverUri + path), HttpVersion.HTTP_2, httpFields);
        HeadersFrame headersFrame = new HeadersFrame(0, requestMetaData, null, true);
        session.newStream(headersFrame, new Promise.Adapter<>(), responseListener);
    }
}
