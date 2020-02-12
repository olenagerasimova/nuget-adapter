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

/**
 * Version of package.
 * See <a href="https://docs.microsoft.com/en-us/nuget/concepts/package-versioning">Package versioning</a>.
 *
 * @todo: normalized() method should actually do normalization as described in docs
 * @since 0.1
 */
public final class Version {

    /**
     * Raw version string.
     */
    private final String raw;

    /**
     * Ctor.
     *
     * @param raw Raw version string.
     */
    public Version(final String raw) {
        this.raw = raw;
    }

    /**
     * Get normalized version.
     * See <a href="https://docs.microsoft.com/en-us/nuget/concepts/package-versioning#normalized-version-numbers">Normalized version numbers</a>.
     *
     * @return Normalized version string.
     */
    public String normalized() {
        return this.raw;
    }
}
