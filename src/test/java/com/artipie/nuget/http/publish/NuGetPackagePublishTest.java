/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
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
import com.artipie.nuget.AstoRepository;
import com.artipie.nuget.http.NuGet;
import com.artipie.nuget.http.TestAuthentication;
import com.artipie.security.policy.PolicyByUsername;
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
            new AstoRepository(new InMemoryStorage()),
            new PolicyByUsername(TestAuthentication.USERNAME),
            new TestAuthentication(),
            "test"
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
            new ResponseMatcher(
                RsStatus.UNAUTHORIZED, new Header("WWW-Authenticate", "Basic realm=\"artipie\"")
            )
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
