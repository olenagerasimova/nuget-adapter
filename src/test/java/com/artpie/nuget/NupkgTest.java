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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Nupkg}.
 *
 * @since 0.1
 */
class NupkgTest {

    @Test
    void shouldCalculateHash(final @TempDir Path temp) throws Exception {
        final BlockingStorage storage = new BlockingStorage(new FileStorage(temp));
        final PackageIdentity identity = new PackageIdentity("foo", "1.0.0");
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
}
