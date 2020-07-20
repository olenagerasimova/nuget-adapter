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

import com.artipie.asto.Content;
import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.StandardRs;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Response with body. Adds no headers as opposite to {@link com.artipie.http.rs.RsWithBody}.
 * Used because `nuget` command line utility for Linux
 * fails to read JSON responses when `Content-Length` header presents.
 *
 * @since 0.3
 */
public final class RsWithBodyNoHeaders implements Response {

    /**
     * Origin response.
     */
    private final Response origin;

    /**
     * Body content.
     */
    private final Content body;

    /**
     * Creates new response from byte buffer.
     *
     * @param bytes Body bytes
     */
    public RsWithBodyNoHeaders(final byte[] bytes) {
        this(StandardRs.EMPTY, bytes);
    }

    /**
     * Decorates origin response body with byte buffer.
     *
     * @param origin Response
     * @param bytes Body bytes
     */
    public RsWithBodyNoHeaders(final Response origin, final byte[] bytes) {
        this(origin, new Content.From(bytes));
    }

    /**
     * Decorates origin response body with content.
     *
     * @param origin Response
     * @param body Content
     */
    public RsWithBodyNoHeaders(final Response origin, final Content body) {
        this.origin = origin;
        this.body = body;
    }

    @Override
    public CompletionStage<Void> send(final Connection con) {
        return this.origin.send(new ConWithBody(con, this.body));
    }

    /**
     * Connection with body publisher.
     *
     * @since 0.3
     */
    private static final class ConWithBody implements Connection {

        /**
         * Origin connection.
         */
        private final Connection origin;

        /**
         * Body publisher.
         */
        private final Publisher<ByteBuffer> body;

        /**
         * Ctor.
         *
         * @param origin Connection
         * @param body Publisher
         */
        ConWithBody(final Connection origin, final Publisher<ByteBuffer> body) {
            this.origin = origin;
            this.body = body;
        }

        @Override
        public CompletionStage<Void> accept(
            final RsStatus status,
            final Headers headers,
            final Publisher<ByteBuffer> none) {
            return this.origin.accept(status, headers, this.body);
        }
    }
}
