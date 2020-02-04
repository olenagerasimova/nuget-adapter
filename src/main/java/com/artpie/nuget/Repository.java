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

/**
 * Class representing NuGet repository.
 *
 * @since 0.1
 */
public class Repository {

    /**
     * The storage.
     */
    private final BlockingStorage storage;

    /**
     * Ctor.
     *
     * @param storage Storage to store all repository data.
     */
    public Repository(final BlockingStorage storage) {
        this.storage = storage;
    }

    /**
     * Adds NuGet package in .nupkg file format from storage.
     *
     * @param key Key to find content of .nupkg package.
     */
    public void add(final Key key) {
        if (this.storage == null) {
            throw new UnsupportedOperationException("Storage will be used later");
        }
        final NuGetPackage nupkg = new Nupkg(new Object());
        final Nuspec nuspec = nupkg.nuspec();
        final PackageIdentity id = nuspec.identity();
        nupkg.save(this.storage, id);
        nupkg.hash().save(this.storage, id);
        nuspec.save(this.storage);
    }
}
