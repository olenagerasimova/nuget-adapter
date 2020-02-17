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
import java.util.Locale;

/**
 * Package identifier.
 *
 * @since 0.1
 */
public final class PackageId {

    /**
     * Raw package identifier string.
     */
    private final String raw;

    /**
     * Ctor.
     *
     * @param raw Raw package identifier string.
     */
    public PackageId(final String raw) {
        this.raw = raw;
    }

    /**
     * Get as lowercase string.
     * See <a href="https://docs.microsoft.com/en-us/dotnet/api/system.string.tolowerinvariant?view=netstandard-2.0#System_String_ToLowerInvariant">.NET's System.String.ToLowerInvariant()</a>.
     *
     * @return Id as lowercase string.
     */
    public String lower() {
        return this.raw.toLowerCase(Locale.getDefault());
    }

    /**
     * Get key for package versions registry.
     *
     * @return Get key for package versions registry.
     */
    public Key versionsKey() {
        return new Key.From(this.lower(), "index.json");
    }
}
