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

import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.google.common.io.ByteSource;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Nuspec}.
 *
 * @since 0.1
 */
class NuspecTest {

    /**
     * Resource `newtonsoft.json.nuspec` name.
     */
    private String name;

    /**
     * Nuspec created from resource.
     */
    private Nuspec nuspec;

    @BeforeEach
    void init() throws Exception {
        this.name = "newtonsoft.json.nuspec";
        this.nuspec = new Nuspec(ByteSource.wrap(new NewtonJsonResource(this.name).bytes()));
    }

    @Test
    void shouldExtractPackageId() throws Exception {
        MatcherAssert.assertThat(
            this.nuspec.packageId().lower(),
            Matchers.equalTo("newtonsoft.json")
        );
    }

    @Test
    void shouldExtractIdentity() throws Exception {
        final PackageIdentity identity = this.nuspec.identity();
        MatcherAssert.assertThat(
            identity.nuspecKey().string(),
            Matchers.equalTo("newtonsoft.json/12.0.3/newtonsoft.json.nuspec")
        );
    }

    @Test
    void shouldSave(final @TempDir Path temp) throws Exception {
        final BlockingStorage storage = new BlockingStorage(new FileStorage(temp));
        this.nuspec.save(storage);
        MatcherAssert.assertThat(
            storage.value(this.nuspec.identity().nuspecKey()),
            Matchers.equalTo(new NewtonJsonResource(this.name).bytes())
        );
    }
}
