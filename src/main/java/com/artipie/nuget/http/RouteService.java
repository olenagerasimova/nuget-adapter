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

import com.artipie.nuget.http.index.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Service that is located by {@link Route}.
 *
 * @since 0.1
 */
final class RouteService implements Service {

    /**
     * Base URL for the route.
     */
    private final URL base;

    /**
     * Route for the service.
     */
    private final Route route;

    /**
     * Service type.
     */
    private final String stype;

    /**
     * Ctor.
     *
     * @param base Base URL for the route.
     * @param route Route for the service.
     * @param stype Service type.
     */
    RouteService(final URL base, final Route route, final String stype) {
        this.base = base;
        this.route = route;
        this.stype = stype;
    }

    @Override
    public String url() {
        final String path = String.format("%s%s", this.base.getPath(), this.route.path());
        final String file = Optional.ofNullable(this.base.getQuery())
            .map(query -> String.format("%s?%s", path, this.base.getQuery()))
            .orElse(path);
        try {
            return new URL(this.base.getProtocol(), this.base.getHost(), this.base.getPort(), file)
                .toString();
        } catch (final MalformedURLException ex) {
            throw new IllegalStateException(
                String.format("Failed to build URL from base: '%s'", this.base),
                ex
            );
        }
    }

    @Override
    public String type() {
        return this.stype;
    }
}
