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

public class PushMessage {

    private final String data;
    private final String path;
    private final LocalDateTime createdDateTime;
    private final LocalDateTime receivedDateTime;

    public PushMessage(final String data,
                       final String path,
                       final LocalDateTime createdDateTime,
                       final LocalDateTime receivedDateTime) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(path, "path");
        this.data = data;
        this.path = path;
        this.createdDateTime = createdDateTime;
        this.receivedDateTime = receivedDateTime;
    }

    public String data() {
        return data;
    }

    public String path() {
        return path;
    }

    public LocalDateTime createdDateTime() {
        return createdDateTime;
    }

    public LocalDateTime receivedDateTime() {
        return receivedDateTime;
    }
}
