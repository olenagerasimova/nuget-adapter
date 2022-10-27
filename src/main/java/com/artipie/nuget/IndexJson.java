/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Index json operations. Index json is metadata file for various version of NuGet package, it's
 * called registration page in the repository docs.
 * <a href="https://learn.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-page-object">Registration page</a>.
 * @since 1.5
 */
public interface IndexJson {

    /**
     * Update (or create) index.json metadata by adding
     * package info from NUSPEC package metadata.
     * @since 1.5
     */
    final class Update {

        /**
         * Optional input stream with existing index json metadata.
         */
        private final Optional<InputStream> input;

        /**
         * Output stream where to write the result.
         */
        private final OutputStream output;

        /**
         * Primary ctor.
         * @param input Optional input stream with existing index json metadata
         * @param output Output stream where to write the result
         */
        public Update(final Optional<InputStream> input, final OutputStream output) {
            this.input = input;
            this.output = output;
        }

        /**
         * Ctor with empty optional for input stream.
         * @param output Output stream where to write the result
         */
        public Update(final OutputStream output) {
            this(Optional.empty(), output);
        }

        /**
         * Ctor.
         * @param input Optional input stream with existing index json metadata
         * @param output Output stream where to write the result
         */
        public Update(final InputStream input, final OutputStream output) {
            this(Optional.of(input), output);
        }

        /**
         * Creates or updates index.json by adding information about new provided package. If such
         * package version already exists in index.json, package metadata are replaced.
         * {@link  NuGetPackage} instance can be created from NuGet package input stream by calling
         * constructor {@link Nupkg#Nupkg(InputStream)}.
         * @param pkg New package to add
         * @param urls Urls required to build index.json
         */
        void update(final NuGetPackage pkg, final Urls urls) {
            throw new NotImplementedException("Not implemented yet");
        }
    }

    /**
     * Various urls to build index json metadata.
     * @since 1.5
     */
    final class Urls {

        /**
         * The URL to the registration page, @id field for registration page object.
         * <a href="https://learn.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-page-object">Docs.</a>
         */
        private final String indexJsonId;

        /**
         * The URL to the package content (.nupkg), packageContent field of the
         * registration leaf object in a page.
         * <a href="https://learn.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-leaf-object-in-a-page">Docs.</a>
         */
        private final String packageContent;

        /**
         * The URL to the document used to produce this object, @id field for catalogEntry item.
         * <a href="https://learn.microsoft.com/en-us/nuget/api/registration-base-url-resource#catalog-entry">Docs.</a>
         */
        private final String catalogEntryId;

        /**
         * Ctor.
         * @param indexJsonId The URL to the registration page
         * @param packageContent The URL to the package content (.nupkg)
         * @param catalogEntryId The URL to the document used to produce this object
         */
        public Urls(final String indexJsonId, final String packageContent,
            final String catalogEntryId) {
            this.indexJsonId = indexJsonId;
            this.packageContent = packageContent;
            this.catalogEntryId = catalogEntryId;
        }
    }

}
