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
import com.artipie.asto.ext.PublisherAs;
import com.google.common.io.ByteSource;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
     * @return Completion of adding package.
     */
    public CompletionStage<Void> add(final Key key) {
        return this.storage.value(key)
            .thenApply(PublisherAs::new)
            .thenCompose(PublisherAs::bytes)
            .thenApply(bytes -> new Nupkg(ByteSource.wrap(bytes)))
            .thenCompose(
                nupkg -> {
                    final Nuspec nuspec;
                    final PackageIdentity id;
                    try {
                        nuspec = nupkg.nuspec();
                        id = nuspec.identity();
                    } catch (final UncheckedIOException | IllegalArgumentException ex) {
                        throw new InvalidPackageException(ex);
                    }
                    return this.storage.list(id.rootKey()).thenCompose(
                        existing -> {
                            if (!existing.isEmpty()) {
                                throw new PackageVersionAlreadyExistsException(id.toString());
                            }
                            return this.storage.exclusively(
                                nuspec.packageId().rootKey(),
                                target -> {
                                    final CompletionStage<Versions> versions;
                                    versions = this.versions(nuspec.packageId());
                                    return CompletableFuture.allOf(
                                        target.move(key, id.nupkgKey()),
                                        nupkg.hash().save(target, id).toCompletableFuture(),
                                        nuspec.save(target).toCompletableFuture()
                                    ).thenCompose(nothing -> versions).thenApply(
                                        vers -> vers.add(nuspec.version())
                                    ).thenCompose(
                                        vers -> vers.save(target, nuspec.packageId().versionsKey())
                                    );
                                }
                            );
                        }
                    );
                }
            );
    }

    /**
     * Enumerates package versions.
     *
     * @param id Package identifier.
     * @return Versions of package.
     */
    public CompletionStage<Versions> versions(final PackageId id) {
        final Key key = id.versionsKey();
        return this.storage.exists(key).thenCompose(
            exists -> {
                final CompletionStage<Versions> versions;
                if (exists) {
                    versions = this.storage.value(key)
                        .thenApply(PublisherAs::new)
                        .thenCompose(PublisherAs::bytes)
                        .thenApply(ByteSource::wrap)
                        .thenApply(Versions::new);
                } else {
                    versions = CompletableFuture.completedFuture(new Versions());
                }
                return versions;
            }
        );
    }

    /**
     * Read package description in .nuspec format.
     *
     * @param identity Package identity consisting of package id and version.
     * @return Package description in .nuspec format.
     */
    public CompletionStage<Nuspec> nuspec(final PackageIdentity identity) {
        return this.storage.exists(identity.nuspecKey()).thenCompose(
            exists -> {
                if (!exists) {
                    throw new IllegalArgumentException(
                        String.format("Cannot find package: %s", identity)
                    );
                }
                return this.storage.value(identity.nuspecKey())
                    .thenApply(PublisherAs::new)
                    .thenCompose(PublisherAs::bytes)
                    .thenApply(bytes -> new Nuspec(ByteSource.wrap(bytes)));
            }
        );
    }
}
