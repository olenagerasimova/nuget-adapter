/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Key;
import java.util.Locale;

/**
 * Package identifier.
 *
 * @since 0.1
 */
public final class PackageId {

    /**
     * Raw package identifier string.
     */
    private final String raw;

    /**
     * Ctor.
     *
     * @param raw Raw package identifier string.
     */
    public PackageId(final String raw) {
        this.raw = raw;
    }

    /**
     * Get as case-sensitive original string.
     *
     * @return Original string.
     */
    public String original() {
        return this.raw;
    }

    /**
     * Get as lowercase string.
     * See <a href="https://docs.microsoft.com/en-us/dotnet/api/system.string.tolowerinvariant?view=netstandard-2.0#System_String_ToLowerInvariant">.NET's System.String.ToLowerInvariant()</a>.
     *
     * @return Id as lowercase string.
     */
    public String lower() {
        return this.raw.toLowerCase(Locale.getDefault());
    }

    /**
     * Get key for package root.
     *
     * @return Key for package root.
     */
    public Key rootKey() {
        return new Key.From(this.lower());
    }

    /**
     * Get key for package versions registry.
     *
     * @return Get key for package versions registry.
     */
    public Key versionsKey() {
        return new Key.From(this.rootKey(), "index.json");
    }

    @Override
    public String toString() {
        return this.raw;
    }
}
