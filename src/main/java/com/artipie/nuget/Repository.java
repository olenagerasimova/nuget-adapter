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

package com.artipie.nuget;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletionException;

/**
 * Class representing NuGet repository.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Repository {

    /**
     * The storage.
     */
    private final Storage storage;

    /**
     * Ctor.
     *
     * @param storage Storage to store all repository data.
     */
    public Repository(final Storage storage) {
        this.storage = storage;
    }

    /**
     * Adds NuGet package in .nupkg file format from storage.
     *
     * @param key Key to find content of .nupkg package.
     * @throws IOException In case exception occurred on operations with storage.
     * @throws InterruptedException In case executing thread has been interrupted.
     * @throws InvalidPackageException If package content is invalid and so cannot be added.
     * @throws PackageVersionAlreadyExistsException If package version already in storage.
     */
    public void add(final Key key)
        throws IOException, InterruptedException,
        InvalidPackageException, PackageVersionAlreadyExistsException {
        final NuGetPackage nupkg = new Nupkg(
            ByteSource.wrap(new BlockingStorage(this.storage).value(key))
        );
        final Nuspec nuspec;
        final PackageIdentity id;
        try {
            nuspec = nupkg.nuspec();
            id = nuspec.identity();
        } catch (final IOException | IllegalArgumentException ex) {
            throw new InvalidPackageException(ex);
        }
        if (!new BlockingStorage(this.storage).list(id.rootKey()).isEmpty()) {
            throw new PackageVersionAlreadyExistsException(id.toString());
        }
        this.storage.exclusively(
            nuspec.packageId().rootKey(),
            target -> target.move(key, id.nupkgKey())
                .thenCompose(nothing -> nupkg.hash().save(target, id))
                .thenRun(
                    () -> {
                        final BlockingStorage blocking = new BlockingStorage(target);
                        try {
                            nuspec.save(blocking);
                            final Versions versions = this.versions(nuspec.packageId());
                            versions.add(nuspec.version()).save(
                                blocking,
                                nuspec.packageId().versionsKey()
                            );
                        } catch (final IOException ex) {
                            throw new UncheckedIOException(ex);
                        } catch (final InterruptedException ex) {
                            throw new CompletionException(ex);
                        }
                    }
                )
        ).toCompletableFuture().join();
    }

    /**
     * Enumerates package versions.
     *
     * @param id Package identifier.
     * @return Versions of package.
     * @throws InterruptedException In case executing thread has been interrupted.
     */
    public Versions versions(final PackageId id) throws InterruptedException {
        final BlockingStorage blocking = new BlockingStorage(this.storage);
        final Key key = id.versionsKey();
        final Versions versions;
        if (blocking.exists(key)) {
            versions = new Versions(ByteSource.wrap(blocking.value(key)));
        } else {
            versions = new Versions();
        }
        return versions;
    }

    /**
     * Read package description in .nuspec format.
     *
     * @param identity Package identity consisting of package id and version.
     * @return Package description in .nuspec format.
     * @throws InterruptedException In case executing thread has been interrupted.
     */
    public Nuspec nuspec(final PackageIdentity identity) throws InterruptedException {
        final BlockingStorage blocking = new BlockingStorage(this.storage);
        if (!blocking.exists(identity.nuspecKey())) {
            throw new IllegalArgumentException(String.format("Cannot find package: %s", identity));
        }
        return new Nuspec(ByteSource.wrap(blocking.value(identity.nuspecKey())));
    }
}
