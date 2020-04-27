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
package com.artipie.nuget;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonString;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Versions}.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class VersionsTest {

    /**
     * Storage used in tests.
     */
    private BlockingStorage storage;

    @BeforeEach
    void init() {
        this.storage = new BlockingStorage(new InMemoryStorage());
    }

    @Test
    void shouldAddVersionWhenEmpty() throws Exception {
        final Version version = new Version("0.1.0");
        final List<String> versions = this.addVersionTo("{\"versions\":[]}", version);
        MatcherAssert.assertThat(
            versions,
            Matchers.equalTo(Collections.singletonList(version.normalized()))
        );
    }

    @Test
    void shouldAddVersionWhenNotEmpty() throws Exception {
        final Version version = new Version("1.1.0");
        final List<String> versions = this.addVersionTo("{\"versions\":[\"1.0.0\"]}", version);
        MatcherAssert.assertThat(
            versions,
            Matchers.equalTo(Arrays.asList("1.0.0", version.normalized()))
        );
    }

    @Test
    void shouldGetAllVersionsWhenEmpty() throws Exception {
        final Versions versions = new Versions(
            ByteSource.wrap("{ \"versions\":[] }".getBytes())
        );
        MatcherAssert.assertThat(versions.all(), new IsEmptyCollection<>());
    }

    @Test
    void shouldGetAllVersionsOrdered() throws Exception {
        final Versions versions = new Versions(
            ByteSource.wrap("{ \"versions\":[\"1.0.1\",\"0.1\",\"2.0\",\"1.0\"] }".getBytes())
        );
        MatcherAssert.assertThat(
            versions.all().stream().map(Version::normalized).collect(Collectors.toList()),
            new IsEqual<>(Arrays.asList("0.1", "1.0", "1.0.1", "2.0"))
        );
    }

    @Test
    void shouldSave() throws Exception {
        final Key.From key = new Key.From("foo");
        final byte[] data = "data".getBytes();
        new Versions(ByteSource.wrap(data)).save(this.storage, key);
        MatcherAssert.assertThat(
            "Saved versions are not identical to versions initial content",
            this.storage.value(key),
            new IsEqual<>(data)
        );
    }

    @Test
    void shouldSaveEmpty() throws Exception {
        final Key.From key = new Key.From("bar");
        new Versions().save(this.storage, key);
        MatcherAssert.assertThat(
            "Versions created from scratch expected to be empty",
            this.versions(key),
            new IsEmptyCollection<>()
        );
    }

    private List<String> addVersionTo(final String original, final Version version)
        throws Exception {
        final Versions versions = new Versions(ByteSource.wrap(original.getBytes()));
        final Key.From sink = new Key.From("sink");
        versions.add(version).save(this.storage, sink);
        return this.versions(sink);
    }

    private List<String> versions(final Key key) {
        final byte[] bytes = this.storage.value(key);
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
            return reader.readObject()
                .getJsonArray("versions")
                .getValuesAs(JsonString.class)
                .stream()
                .map(JsonString::getString)
                .collect(Collectors.toList());
        }
    }
}
