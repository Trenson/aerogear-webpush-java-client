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

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ParseUtils {

    private static final Pattern REL = Pattern.compile("\\\"(.*?)\\\"");
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private ParseUtils() {}

    public static String parseLink(final List<String> linkHeaders, final String rel) {
        Objects.requireNonNull(linkHeaders, "linkHeaders");
        Objects.requireNonNull(rel, "rel");
        for (final String link : linkHeaders) {
            Matcher matcher = REL.matcher(link);
            if (matcher.find() && rel.equals(matcher.group(1))) {
                return link.substring(1, link.lastIndexOf('>'));
            }
        }
        return null;
    }

    public static Long parseMaxAge(final CharSequence cacheControl) {
        Matcher matcher = DIGITS.matcher(cacheControl);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(0));
        }
        return null;
    }
}
