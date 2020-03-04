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
import com.artipie.asto.memory.InMemoryStorage;
import com.google.common.io.ByteSource;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Nupkg}.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class NupkgTest {

    /**
     * Resource `newtonsoft.json.12.0.3.nupkg` name.
     */
    private String name;

    @BeforeEach
    void init() {
        this.name = "newtonsoft.json.12.0.3.nupkg";
    }

    @Test
    void shouldSave() throws Exception {
        final BlockingStorage storage = new BlockingStorage(new InMemoryStorage());
        final String id = "newtonsoft.json";
        final String version = "12.0.3";
        new Nupkg(ByteSource.wrap(new NewtonJsonResource(this.name).bytes()))
            .save(storage, new PackageIdentity(new PackageId(id), new Version(version)));
        MatcherAssert.assertThat(
            storage.value(new Key.From(id, version, this.name)),
            Matchers.equalTo(new NewtonJsonResource(this.name).bytes())
        );
    }

    @Test
    void shouldCalculateHash() throws Exception {
        final BlockingStorage storage = new BlockingStorage(new InMemoryStorage());
        final PackageIdentity identity = new PackageIdentity(
            new PackageId("foo"),
            new Version("1.0.0")
        );
        new Nupkg(ByteSource.wrap("test data".getBytes(StandardCharsets.UTF_8)))
            .hash()
            .save(storage, identity);
        MatcherAssert.assertThat(
            new String(storage.value(identity.hashKey())),
            Matchers.equalTo(
                // @checkstyle LineLength (1 lines)
                "Dh4h7PEF7IU9JNcohnrXBhPCFmOkaTB0sqNhnBvTnWa1iMM3I7tGbHJCToDjymPCSQeKs0e6uUKFAOfuQwWdDQ=="
            )
        );
    }

    @Test
    void shouldExtractNuspec() throws Exception {
        final Nuspec nuspec = new Nupkg(
            ByteSource.wrap(new NewtonJsonResource(this.name).bytes())
        ).nuspec();
        MatcherAssert.assertThat(
            nuspec.identity().nupkgKey().string(),
            Matchers.is("newtonsoft.json/12.0.3/newtonsoft.json.12.0.3.nupkg")
        );
    }
}
