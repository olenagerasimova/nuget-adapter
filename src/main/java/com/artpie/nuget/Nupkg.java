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

/**
 * Package in .nupkg format.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Nupkg {

    /**
     * Binary content of package.
     */
    private final Object content;

    /**
     * Ctor.
     *
     * @param content Binary content of package.
     */
    public Nupkg(final Object content) {
        this.content = content;
    }

    /**
     * Parses binary content of package.
     *
     * @return Parsed package.
     */
    public ParsedNupkg parse() {
        if (this.content == null) {
            throw new UnsupportedOperationException("Content will be used later");
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Calculates hash of package binary content using SHA512 algorithm encoded in Base64.
     *
     * @return Package hash.
     */
    public Hash hash() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Saves package binary content to storage.
     *
     * @param storage Storage to use for saving.
     * @param identity Package identity.
     */
    public void save(final BlockingStorage storage, final PackageIdentity identity) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
