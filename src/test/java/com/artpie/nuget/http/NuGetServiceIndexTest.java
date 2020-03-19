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

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.Response;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NuGet}.
 * Service index resource.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class NuGetServiceIndexTest {

    /**
     * Base URL for services.
     */
    private URL url;

    /**
     * Tested NuGet slice.
     */
    private NuGet nuget;

    @BeforeEach
    void init() throws Exception {
        this.url = new URL("http://localhost:4321/repo");
        this.nuget = new NuGet(this.url, "/base", new InMemoryStorage());
    }

    @Test
    void shouldGetIndex() {
        final Response response = this.nuget.response(
            "GET /base/index.json",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(
                        new IsValidServiceIndex(
                            new IsIterableContainingInAnyOrder<>(
                                Arrays.asList(
                                    new IsService(
                                        "PackagePublish/2.0.0",
                                        String.format("%s/package", this.url)
                                    ),
                                    new IsService(
                                        "PackageBaseAddress/3.0.0",
                                        String.format("%s/content", this.url)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    void shouldFailPutIndex() {
        final Response response = this.nuget.response(
            "PUT /base/index.json",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(response, new RsHasStatus(RsStatus.METHOD_NOT_ALLOWED));
    }

    /**
     * Matcher for bytes array representing valid Service Index JSON.
     *
     * @since 0.1
     */
    private class IsValidServiceIndex extends TypeSafeMatcher<byte[]> {

        /**
         * Matcher for services list.
         */
        private final Matcher<Iterable<? extends JsonObject>> services;

        IsValidServiceIndex(final Matcher<Iterable<? extends JsonObject>> services) {
            this.services = services;
        }

        @Override
        public void describeTo(final Description description) {
            description
                .appendText("Service Index JSON with services ")
                .appendDescriptionOf(this.services);
        }

        @Override
        public boolean matchesSafely(final byte[] bytes) {
            final JsonObject root;
            try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
                root = reader.readObject();
            }
            return root.getString("version").equals("3.0.0")
                && this.services.matches(
                root.getJsonArray("resources").getValuesAs(JsonObject.class)
            );
        }
    }

    /**
     * Matcher for JSON object representing service.
     *
     * @since 0.1
     */
    private class IsService extends TypeSafeMatcher<JsonObject> {

        /**
         * Expected service type.
         */
        private final String type;

        /**
         * Expected service id.
         */
        private final String id;

        IsService(final String type, final String id) {
            this.type = type;
            this.id = id;
        }

        @Override
        public void describeTo(final Description description) {
            description
                .appendText("service with type=").appendText(this.type)
                .appendText(" and id=").appendText(this.id);
        }

        @Override
        protected boolean matchesSafely(final JsonObject obj) {
            return obj.getString("@type").equals(this.type) && obj.getString("@id").equals(this.id);
        }
    }
}
