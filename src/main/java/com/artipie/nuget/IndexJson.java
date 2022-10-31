/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import java.io.InputStream;
import java.util.Optional;
import javax.json.JsonObject;
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
         * Primary ctor.
         * @param input Optional input stream with existing index json metadata
         */
        public Update(final Optional<InputStream> input) {
            this.input = input;
        }

        /**
         * Ctor with empty optional for input stream.
         */
        public Update() {
            this(Optional.empty());
        }

        /**
         * Ctor.
         * @param input Optional input stream with existing index json metadata
         */
        public Update(final InputStream input) {
            this(Optional.of(input));
        }

        /**
         * Creates or updates index.json by adding information about new provided package. If such
         * package version already exists in index.json, package metadata are replaced.
         * {@link  NuGetPackage} instance can be created from NuGet package input stream by calling
         * constructor {@link Nupkg#Nupkg(InputStream)}.
         * In the resulting json object catalogEntries are placed in the ascending order by
         * package version. Required url fields (like @id, packageContent, @id of the catalogEntry)
         * are set to "null" string.
         * @param pkg New package to add
         * @return Updated index.json metadata as {@link JsonObject}
         */
        JsonObject perform(final NuGetPackage pkg) {
            throw new NotImplementedException("Not implemented yet");
        }
    }

}
