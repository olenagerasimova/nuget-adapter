/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * NuGet repository.
 *
 * @since 0.5
 */
public interface Repository {

    /**
     * Read package content.
     *
     * @param key Package content key.
     * @return Content if exists, empty otherwise.
     */
    CompletionStage<Optional<Content>> content(Key key);

    /**
     * Adds NuGet package in .nupkg file format from storage.
     *
     * @param content Content of .nupkg package.
     * @return Completion of adding package.
     */
    CompletionStage<Void> add(Content content);

    /**
     * Enumerates package versions.
     *
     * @param id Package identifier.
     * @return Versions of package.
     */
    CompletionStage<Versions> versions(PackageId id);

    /**
     * Read package description in .nuspec format.
     *
     * @param identity Package identity consisting of package id and version.
     * @return Package description in .nuspec format.
     */
    CompletionStage<Nuspec> nuspec(PackageIdentity identity);
}
