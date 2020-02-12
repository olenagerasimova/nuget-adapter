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
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Repository}.
 *
 * @since 0.1
 */
class RepositoryTest {

    // @checkstyle VisibilityModifierCheck (5 lines)
    /**
     * Temporary directory.
     */
    @TempDir
    Path temp;

    @Test
    void shouldAddPackage() throws Exception {
        final BlockingStorage storage = new BlockingStorage(new FileStorage(this.temp));
        final Key.From source = new Key.From("package.zip");
        final String nupkg = "newtonsoft.json.12.0.3.nupkg";
        storage.save(source, new NewtonJsonResource(nupkg).bytes());
        final Repository repository = new Repository(storage);
        repository.add(source);
        final Key.From root = new Key.From("newtonsoft.json", "12.0.3");
        MatcherAssert.assertThat(
            storage.value(new Key.From(root, nupkg)),
            Matchers.equalTo(new NewtonJsonResource(nupkg).bytes())
        );
        MatcherAssert.assertThat(
            new String(
                storage.value(new Key.From(root, "newtonsoft.json.12.0.3.nupkg.sha512"))
            ),
            Matchers.equalTo(
                // @checkstyle LineLength (1 lines)
                "aTRmXwR5xYu+mWxE8r8W1DWnL02SeV8LwdQMsLwTWP8OZgrCCyTqvOAe5hRb1VNQYXjln7qr0PKpSyO/pcc19Q=="
            )
        );
        final String nuspec = "newtonsoft.json.nuspec";
        MatcherAssert.assertThat(
            storage.value(new Key.From(root, nuspec)),
            Matchers.equalTo(new NewtonJsonResource(nuspec).bytes())
        );
    }
}
