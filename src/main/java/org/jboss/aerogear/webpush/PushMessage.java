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

public final class PushMessage implements Serializable {

    private static final long serialVersionUID = -5064247285257029828L;

    private final String path;
    private final String data;
    private final LocalDateTime createdDateTime;
    private final LocalDateTime receivedDateTime;

    private PushMessage(final Builder builder) {
        path = Objects.requireNonNull(builder.path, "path");
        if (builder.data.length() == 0) {
            throw new IllegalArgumentException("data is empty");
        }
        data = builder.data.toString();
        createdDateTime = builder.createdDateTime;
        receivedDateTime = builder.receivedDateTime;
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
                "\n\tpath='" + path + "'," +
                "\n\tdata='" + data + "'," +
                "\n\tcreatedDateTime=" + createdDateTime + ',' +
                "\n\treceivedDateTime=" + receivedDateTime +
                "\n}";
    }

    public static class Builder {

        private final String path;
        private StringBuilder data = new StringBuilder();
        private LocalDateTime createdDateTime;
        private LocalDateTime receivedDateTime;

        public Builder(final String path) {
            this.path = path;
        }

        public Builder addDataFrame(final String data) {
            this.data.append(data);
            return this;
        }

        public Builder createdDateTime(final LocalDateTime createdDateTime) {
            this.createdDateTime = createdDateTime;
            return this;
        }

        public Builder receivedDateTime(final LocalDateTime receivedDateTime) {
            this.receivedDateTime = receivedDateTime;
            return this;
        }

        public PushMessage build() {
            return new PushMessage(this);
        }
    }
}
