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
package com.artpie.nuget.http;

import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.Flow;

/**
 * NuGet repository HTTP front end.
 *
 * @since 0.1
 * @todo #19:30min Replace HTTP status magic numbers.
 */
public final class NuGet implements Slice {

    /**
     * Base path.
     */
    private final String base;

    /**
     * Ctor.
     *
     * @param base Base path.
     */
    public NuGet(final String base) {
        this.base = base;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Flow.Publisher<ByteBuffer> body
    ) {
        final Response response;
        final RequestLineFrom request = new RequestLineFrom(line);
        final String path = request.uri().getPath();
        if (path.startsWith(this.base)) {
            final Resource resource = new PackageContent();
            if (request.method().equals("GET")) {
                response = resource.get();
            } else {
                final int notallowed = 405;
                response = new RsWithStatus(notallowed);
            }
        } else {
            final int notfound = 404;
            response = new RsWithStatus(notfound);
        }
        return response;
    }
}
