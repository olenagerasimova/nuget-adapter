/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget.http.metadata;

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.nuget.AstoRepository;
import com.artipie.nuget.Nuspec;
import com.artipie.nuget.PackageId;
import com.artipie.nuget.Version;
import com.artipie.nuget.Versions;
import com.artipie.nuget.http.NuGet;
import com.artipie.nuget.http.TestAuthentication;
import com.artipie.nuget.http.TestPermissions;
import com.google.common.io.ByteSource;
import io.reactivex.Flowable;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NuGet}.
 * Package metadata resource.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class NuGetPackageMetadataTest {

    /**
     * Tested NuGet slice.
     */
    private NuGet nuget;

    /**
     * Storage used by repository.
     */
    private InMemoryStorage storage;

    @BeforeEach
    void init() throws Exception {
        this.storage = new InMemoryStorage();
        this.nuget = new NuGet(
            new URL("http://localhost:4321/repo"),
            new AstoRepository(this.storage),
            new TestPermissions.Read(TestAuthentication.USERNAME),
            new TestAuthentication()
        );
    }

    @Test
    void shouldGetRegistration() {
        new Versions()
            .add(new Version("12.0.3"))
            .save(
                this.storage,
                new PackageId("Newtonsoft.Json").versionsKey()
            );
        new Nuspec(
            ByteSource.wrap(
                String.join(
                    "",
                    "<?xml version=\"1.0\"?>",
                    "<package xmlns=\"http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd\">",
                    "<metadata><id>Newtonsoft.Json</id><version>12.0.3</version></metadata>",
                    "</package>"
                ).getBytes()
            )
        ).save(this.storage);
        final Response response = this.nuget.response(
            new RequestLine(
                RqMethod.GET,
                "/registrations/newtonsoft.json/index.json"
            ).toString(),
            new TestAuthentication.Headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(new IsValidRegistration())
                )
            )
        );
    }

    @Test
    void shouldGetRegistrationsWhenEmpty() {
        final Response response = this.nuget.response(
            new RequestLine(
                RqMethod.GET,
                "/registrations/my.lib/index.json"
            ).toString(),
            new TestAuthentication.Headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(new IsValidRegistration())
                )
            )
        );
    }

    @Test
    void shouldFailPutRegistration() {
        final Response response = this.nuget.response(
            new RequestLine(
                RqMethod.PUT,
                "/registrations/newtonsoft.json/index.json"
            ).toString(),
            new TestAuthentication.Headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(response, new RsHasStatus(RsStatus.METHOD_NOT_ALLOWED));
    }

    @Test
    void shouldFailGetRegistrationWithoutAuth() {
        MatcherAssert.assertThat(
            this.nuget.response(
                new RequestLine(
                    RqMethod.GET,
                    "/registrations/my-utils/index.json"
                ).toString(),
                Headers.EMPTY,
                Flowable.empty()
            ),
            new ResponseMatcher(RsStatus.UNAUTHORIZED, new Header("WWW-Authenticate", "Basic"))
        );
    }

    /**
     * Matcher for bytes array representing valid Registration JSON.
     *
     * @since 0.1
     */
    private static class IsValidRegistration extends TypeSafeMatcher<byte[]> {

        @Override
        public void describeTo(final Description description) {
            description.appendText("is registration JSON");
        }

        @Override
        public boolean matchesSafely(final byte[] bytes) {
            final JsonObject root;
            try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
                root = reader.readObject();
            }
            return root.getInt("count") == root.getJsonArray("items").size();
        }
    }
}
