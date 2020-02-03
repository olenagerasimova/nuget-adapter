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
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Nupkg}.
 *
 * @since 0.1
 */
class NupkgTest {

    /**
     * Storage used in tests.
     */
    private BlockingStorage storage;

    /**
     * Nuspec created from resource.
     */
    private Nupkg nupkg;

    @BeforeEach
    void init() throws Exception {
        this.storage = new BlockingStorage(
            new FileStorage(
                Files.createTempDirectory(NupkgTest.class.getName()).resolve("repo")
            )
        );
        final byte[] bytes = new NewtonJsonResource("newtonsoft.json.12.0.3.nupkg").bytes();
        this.nupkg = new Nupkg(ByteSource.wrap(bytes));
    }

    @Test
    void shouldCalculateHash() throws Exception {
        final PackageIdentity identity = new PackageIdentity("newtonsoft.json", "12.0.3");
        this.nupkg.hash().save(this.storage, identity);
        MatcherAssert.assertThat(
            new String(this.storage.value(identity.hashKey())),
            Matchers.equalTo(
                // @checkstyle LineLength (1 lines)
                "aTRmXwR5xYu+mWxE8r8W1DWnL02SeV8LwdQMsLwTWP8OZgrCCyTqvOAe5hRb1VNQYXjln7qr0PKpSyO/pcc19Q=="
            )
        );
    }
}
