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

import com.artipie.http.auth.Identities;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.SliceAuth;

/**
 * Route supporting basic authentication.
 *
 * @since 0.2
 */
final class BasicAuthRoute implements Route {

    /**
     * Origin route.
     */
    private final Route origin;

    /**
     * Authorization.
     */
    private final Permission perm;

    /**
     * Authentication.
     */
    private final Identities ids;

    /**
     * Ctor.
     *
     * @param origin Origin route.
     * @param perm Authorization mechanism.
     * @param ids Authentication mechanism.
     */
    BasicAuthRoute(final Route origin, final Permission perm, final Identities ids) {
        this.origin = origin;
        this.ids = ids;
        this.perm = perm;
    }

    @Override
    public String path() {
        return this.origin.path();
    }

    @Override
    public Resource resource(final String path) {
        return new ResourceFromSlice(
            path,
            new SliceAuth(
                new SliceFromResource(this.origin.resource(path)),
                this.perm,
                this.ids
            )
        );
    }
}
