/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.google.common.io.ByteSource;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * NuGet repository that stores packages in {@link Storage}.
 *
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class AstoRepository implements Repository {

    /**
     * The storage.
     */
    private final Storage storage;

    /**
     * Ctor.
     *
     * @param storage Storage to store all repository data.
     */
    public AstoRepository(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public CompletionStage<Optional<Content>> content(final Key key) {
        return this.storage.exists(key).thenCompose(
            exists -> {
                final CompletionStage<Optional<Content>> result;
                if (exists) {
                    result = this.storage.value(key).thenApply(Optional::of);
                } else {
                    result = CompletableFuture.completedFuture(Optional.empty());
                }
                return result;
            }
        );
    }

    @Override
    public CompletionStage<Void> add(final Content content) {
        final Key key = new Key.From(UUID.randomUUID().toString());
        return this.storage.save(key, content).thenCompose(
            saved -> this.storage.value(key)
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
                                            vers -> vers.save(
                                                target,
                                                nuspec.packageId().versionsKey()
                                            )
                                        );
                                    }
                                );
                            }
                        );
                    }
                )
        );
    }

    @Override
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

    @Override
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
