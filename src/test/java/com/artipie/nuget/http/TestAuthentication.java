/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.nuget.http;

import com.artipie.http.Headers;
import com.artipie.http.auth.Authentication;
import com.artipie.http.rs.Header;
import java.util.Optional;

/**
 * Single user basic authentication for usage in tests.
 *
 * @since 0.2
 */
public final class TestAuthentication implements Authentication {

    /**
     * User name.
     */
    public static final String USERNAME = "Aladdin";

    /**
     * Basic authentication header.
     */
    public static final Header HEADER = new Header(
        "Authorization",
        "Basic QWxhZGRpbjpPcGVuU2VzYW1l"
    );

    /**
     * Basic authentication headers.
     */
    public static final Headers HEADERS = new Headers.From(TestAuthentication.HEADER);

    @Override
    public Optional<String> user(final String username, final String password) {
        final Optional<String> auth;
        if (USERNAME.equals(username) && "OpenSesame".equals(password)) {
            auth = Optional.of(username);
        } else {
            auth = Optional.empty();
        }
        return auth;
    }
}
