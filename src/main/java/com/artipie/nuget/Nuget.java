/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

/**
 * Nuget package.
 * @since 0.6
 */
public interface Nuget {

    /**
     * Extracts nuget metadata .nuspec file from the package.
     * @return Instance of {@link Nuspec} class, .nuspec metadata file
     */
    Nuspec extractMetadata();

    /**
     * Calculates hash of package binary content using SHA512 algorithm encoded in Base64.
     * @return Package hash
     */
    String hash();

    /**
     * Nuget package description in .nuspec format, file format is described
     * <a href="https://github.com/NuGet/docs.microsoft.com-nuget/blob/main/docs/reference/nuspec.md">here</a>.
     * @since 0.6
     */
    interface Nuspec {

        /**
         * Package identifier: original case sensitive and lowercase.
         * @return Package id
         * @checkstyle MethodNameCheck (3 lines)
         */
        @SuppressWarnings("PMD.ShortMethodName")
        NuspecField id();

        /**
         * Package versions: original version field value or normalised.
         * @return Version of the package
         */
        NuspecField version();

        /**
         * Package description.
         * @return Description
         */
        String description();

        /**
         * Package authors.
         * @return Authors
         */
        String authors();

    }

    /**
     * Nuspec xml metadata field.
     * @since 0.6
     */
    interface NuspecField {

        /**
         * Original row value (as it was in xml).
         * @return String value
         */
        String row();

        /**
         * Normalized value of the field.
         * @return Normalized value
         */
        String normalised();
    }
}
