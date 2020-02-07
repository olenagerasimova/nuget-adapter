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
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import java.io.IOException;

/**
 * Package in .nupkg format.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Nupkg implements NuGetPackage {

    /**
     * Binary content of package.
     */
    private final ByteSource content;

    /**
     * Ctor.
     *
     * @param content Binary content of package.
     */
    public Nupkg(final ByteSource content) {
        this.content = content;
    }

    @Override
    public Nuspec nuspec() {
        if (this.content == null) {
            throw new UnsupportedOperationException("Content will be used later");
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Hash hash() throws IOException {
        return new Hash(Hashing.sha512().hashBytes(this.content.read()));
    }

    @Override
    public void save(final BlockingStorage storage, final PackageIdentity identity) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
