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

package com.artpie.nuget;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Repository}.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class RepositoryTest {

    /**
     * Storage used in tests.
     */
    private BlockingStorage storage;

    /**
     * Repository to test.
     */
    private Repository repository;

    @BeforeEach
    void init(final @TempDir Path temp) {
        this.storage = new BlockingStorage(new FileStorage(temp));
        this.repository = new Repository(this.storage);
    }

    @Test
    void shouldAddPackage() throws Exception {
        final Key.From source = new Key.From("package.zip");
        final String nupkg = "newtonsoft.json.12.0.3.nupkg";
        this.storage.save(source, new NewtonJsonResource(nupkg).bytes());
        this.repository.add(source);
        final Key.From root = new Key.From("newtonsoft.json", "12.0.3");
        MatcherAssert.assertThat(
            this.storage.value(new Key.From(root, nupkg)),
            Matchers.equalTo(new NewtonJsonResource(nupkg).bytes())
        );
        MatcherAssert.assertThat(
            new String(
                this.storage.value(new Key.From(root, "newtonsoft.json.12.0.3.nupkg.sha512"))
            ),
            Matchers.equalTo(
                // @checkstyle LineLength (1 lines)
                "aTRmXwR5xYu+mWxE8r8W1DWnL02SeV8LwdQMsLwTWP8OZgrCCyTqvOAe5hRb1VNQYXjln7qr0PKpSyO/pcc19Q=="
            )
        );
        final String nuspec = "newtonsoft.json.nuspec";
        MatcherAssert.assertThat(
            this.storage.value(new Key.From(root, nuspec)),
            Matchers.equalTo(new NewtonJsonResource(nuspec).bytes())
        );
    }

    @Test
    void shouldFailToAddInvalidPackage() {
        final Key.From source = new Key.From("invalid");
        this.storage.save(source, "not a zip".getBytes());
        Assertions.assertThrows(
            InvalidPackageException.class,
            () -> this.repository.add(source),
            // @checkstyle LineLengthCheck (1 line)
            "Repository expected to throw InvalidPackageException if package is invalid and cannot be added"
        );
    }

    @Test
    void shouldGetPackageVersions() throws Exception {
        final byte[] bytes = "{\"versions\":[\"1.0.0\",\"1.0.1\"]}"
            .getBytes(StandardCharsets.US_ASCII);
        final PackageId foo = new PackageId("Foo");
        this.storage.save(foo.versionsKey(), bytes);
        final Versions versions = this.repository.versions(foo);
        final Key.From bar = new Key.From("bar");
        versions.save(this.storage, bar);
        MatcherAssert.assertThat(
            "Saved versions are not identical to versions initial content",
            this.storage.value(bar),
            new IsEqual<>(bytes)
        );
    }

    @Test
    void shouldGetEmptyPackageVersionsWhenNonePresent() throws Exception {
        final PackageId pack = new PackageId("MyLib");
        final Versions versions = this.repository.versions(pack);
        final Key.From sink = new Key.From("sink");
        versions.save(this.storage, sink);
        MatcherAssert.assertThat(
            "Versions created from scratch expected to be empty",
            this.versions(sink),
            new IsEmptyCollection<>()
        );
    }

    private JsonArray versions(final Key key) {
        final byte[] bytes = this.storage.value(key);
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
            return reader.readObject().getJsonArray("versions");
        }
    }
}
