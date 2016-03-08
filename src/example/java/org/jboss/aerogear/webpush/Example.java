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

/**
 * For a quick run, debugging and learning purposes only.
 * <p>
 * To run this example add suitable
 * <a href="http://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-versions">alpn-boot</a>
 * to your classpath. For JDK 8u74:
 * {@code -Xbootclasspath/p:${settings.localRepository}/org/mortbay/jetty/alpn/alpn-boot/8.1.7.v20160121/alpn-boot-8.1.7.v20160121.jar}
 */
final class Example {

    public static void main(String[] args) throws Exception {
        WebPushClient webPushClient = new WebPushClient("https://localhost:8443", true);
        try {
            webPushClient.connect();
            webPushClient.subscribe(subscription -> {
                System.out.println(subscription);
                //check new push messages and close the stream (nowait == true)
                webPushClient.monitor(subscription, true, pushMessage -> {
                    if (pushMessage.isPresent()) {
                        System.out.println(pushMessage.get());
                    } else {    //possible only if nowait == true
                        System.out.println("204 No Content");
                    }
                });
                //monitor all new push messages
                webPushClient.monitor(subscription, System.out::println);
            });
            Thread.sleep(30 * 1000);
        } finally {
            webPushClient.disconnect();
        }
    }
}
