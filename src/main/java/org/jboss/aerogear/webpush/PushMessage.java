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

import java.time.LocalDateTime;
import java.util.Objects;

public class PushMessage {  //FIXME should it be serializable?

    private final String path;
    private final String data;
    private final LocalDateTime createdDateTime;
    private final LocalDateTime receivedDateTime;

    public PushMessage(final String path,
                       final String data,
                       final LocalDateTime createdDateTime,
                       final LocalDateTime receivedDateTime) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(data, "data");
        this.path = path;
        this.data = data;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PushMessage)) {
            return false;
        }
        PushMessage that = (PushMessage) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "path='" + path + '\'' +
                ", data='" + data + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", receivedDateTime=" + receivedDateTime +
                '}';
    }
}
