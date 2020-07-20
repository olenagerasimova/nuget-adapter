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
package com.artipie.nuget.http.metadata;

import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rs.RsStatus;
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
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
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
            "/base",
            this.storage,
            new TestPermissions(TestAuthentication.USERNAME, NuGet.READ),
            new TestAuthentication()
        );
    }

    @Test
    void shouldGetRegistration() throws Exception {
        new Versions()
            .add(new Version("12.0.3"))
            .save(
                new BlockingStorage(this.storage),
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
        ).save(new BlockingStorage(this.storage));
        final Response response = this.nuget.response(
            "GET /base/registrations/newtonsoft.json/index.json HTTP/1.1",
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
            "GET /base/registrations/my.lib/index.json HTTP/1.1",
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
            "PUT /base/registrations/newtonsoft.json/index.json HTTP/1.1",
            new TestAuthentication.Headers(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(response, new RsHasStatus(RsStatus.METHOD_NOT_ALLOWED));
    }

    @Test
    void shouldFailGetRegistrationWithoutAuth() {
        MatcherAssert.assertThat(
            this.nuget.response(
                "GET /base/registrations/my-utils/index.json HTTP/1.1",
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
