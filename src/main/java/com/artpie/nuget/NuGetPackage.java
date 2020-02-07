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
import java.io.IOException;

/**
 * NuGet package.
 *
 * @since 0.1
 */
public interface NuGetPackage {

    /**
     * Calculates hash of package binary content using SHA512 algorithm encoded in Base64.
     *
     * @return Package hash.
     * @throws IOException In case exception occurred on reading content.
     */
    Hash hash() throws IOException;

    /**
     * Extract package description in .nuspec format.
     *
     * @return Package description.
     */
    Nuspec nuspec();

    /**
     * Saves package binary content to storage.
     *
     * @param storage Storage to use for saving.
     * @param identity Package identity.
     * @throws IOException In case exception occurred on saving content.
     */
    void save(BlockingStorage storage, PackageIdentity identity) throws IOException;
}
