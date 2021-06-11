/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.nuget.metadata.Nuspec;

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
     */
    Hash hash();

    /**
     * Extract package description in .nuspec format.
     *
     * @return Package description.
     */
    Nuspec nuspec();
}
