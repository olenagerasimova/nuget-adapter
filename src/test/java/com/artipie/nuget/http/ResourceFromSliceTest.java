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
import com.artipie.http.Response;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ResourceFromSlice}.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class ResourceFromSliceTest {

    @Test
    void shouldDelegateGetResponse() {
        final RsStatus status = RsStatus.OK;
        final String path = "/some/path";
        final Header header = new Header("Name", "Value");
        final Response response = new ResourceFromSlice(
            path,
            (line, hdrs, body) -> new RsFull(
                status,
                hdrs,
                Flowable.just(ByteBuffer.wrap(line.getBytes()))
            )
        ).get(new Headers.From(Collections.singleton(header)));
        MatcherAssert.assertThat(
            response,
            Matchers.allOf(
                new RsHasStatus(status),
                new RsHasHeaders(header),
                new RsHasBody(
                    new RequestLine(RqMethod.GET, path).toString().getBytes()
                )
            )
        );
    }

    @Test
    void shouldDelegatePutResponse() {
        final RsStatus status = RsStatus.OK;
        final String path = "/some/other/path";
        final Header header = new Header("X-Name", "Something");
        final String content = "body";
        final Response response = new ResourceFromSlice(
            path,
            (line, hdrs, body) -> new RsFull(
                status,
                hdrs,
                Flowable.concat(Flowable.just(ByteBuffer.wrap(line.getBytes())), body)
            )
        ).put(
            new Headers.From(Collections.singleton(header)),
            Flowable.just(ByteBuffer.wrap(content.getBytes()))
        );
        MatcherAssert.assertThat(
            response,
            Matchers.allOf(
                new RsHasStatus(status),
                new RsHasHeaders(header),
                new RsHasBody(
                    String.join(
                        "",
                        new RequestLine(RqMethod.PUT, path).toString(),
                        content
                    ).getBytes()
                )
            )
        );
    }
}
