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
package com.artipie.nuget.http.publish;

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.nuget.Repository;
import com.artipie.nuget.http.NuGet;
import com.artipie.nuget.http.TestAuthentication;
import com.artipie.nuget.http.TestPermissions;
import com.google.common.io.Resources;
import io.reactivex.Flowable;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NuGet}.
 * Package publish resource.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class NuGetPackagePublishTest {

    /**
     * Tested NuGet slice.
     */
    private NuGet nuget;

    @BeforeEach
    void init() throws Exception {
        this.nuget = new NuGet(
            new URL("http://localhost"),
            new Repository(new InMemoryStorage()),
            new TestPermissions.Write(TestAuthentication.USERNAME),
            new TestAuthentication()
        );
    }

    @Test
    void shouldPutPackagePublish() throws Exception {
        final Response response = this.putPackage(nupkg());
        MatcherAssert.assertThat(
            response,
            new RsHasStatus(RsStatus.CREATED)
        );
    }

    @Test
    void shouldFailPutPackage() throws Exception {
        MatcherAssert.assertThat(
            "Should fail to add package which is not a ZIP archive",
            this.putPackage("not a zip".getBytes()),
            new RsHasStatus(RsStatus.BAD_REQUEST)
        );
    }

    @Test
    void shouldFailPutSamePackage() throws Exception {
        this.putPackage(nupkg()).send(
            (status, headers, body) -> CompletableFuture.allOf()
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Should fail to add same package when it is already present in the repository",
            this.putPackage(nupkg()),
            new RsHasStatus(RsStatus.CONFLICT)
        );
    }

    @Test
    void shouldFailGetPackagePublish() {
        final Response response = this.nuget.response(
            new RequestLine(RqMethod.GET, "/package").toString(),
            new TestAuthentication.Headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(response, new RsHasStatus(RsStatus.METHOD_NOT_ALLOWED));
    }

    @Test
    void shouldFailPutPackageWithoutAuth() {
        MatcherAssert.assertThat(
            this.nuget.response(
                new RequestLine(RqMethod.PUT, "/package").toString(),
                Headers.EMPTY,
                Flowable.fromArray(ByteBuffer.wrap("data".getBytes()))
            ),
            new ResponseMatcher(RsStatus.UNAUTHORIZED, new Header("WWW-Authenticate", "Basic"))
        );
    }

    private Response putPackage(final byte[] pack) throws Exception {
        final HttpEntity entity = MultipartEntityBuilder.create()
            .addBinaryBody("package.nupkg", pack)
            .build();
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        entity.writeTo(sink);
        return this.nuget.response(
            new RequestLine(RqMethod.PUT, "/package").toString(),
            new Headers.From(
                new TestAuthentication.Header(),
                new Header("Content-Type", entity.getContentType().getValue())
            ),
            Flowable.fromArray(ByteBuffer.wrap(sink.toByteArray()))
        );
    }

    private static byte[] nupkg() throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader()
            .getResource("newtonsoft.json/12.0.3/newtonsoft.json.12.0.3.nupkg");
        return Resources.toByteArray(resource);
    }

}
