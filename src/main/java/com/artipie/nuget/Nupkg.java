/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Package in .nupkg format.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Nupkg implements Package {

    /**
     * Binary content of package.
     */
    private final ByteSource content;

    /**
     * Ctor.
     *
     * @param content Binary content of package.
     */
    public Nupkg(final ByteSource content) {
        this.content = content;
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Nuspec nuspec() {
        Nuspec nuspec = null;
        try (ZipInputStream zipStream = new ZipInputStream(this.content.openStream())) {
            while (true) {
                final ZipEntry entry = zipStream.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (entry.getName().endsWith(".nuspec")) {
                    if (nuspec != null) {
                        throw new IllegalArgumentException(
                            "More then one .nuspec file found inside the package."
                        );
                    }
                    nuspec = new Nuspec(ByteSource.wrap(ByteStreams.toByteArray(zipStream)));
                }
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (nuspec == null) {
            throw new IllegalArgumentException("No .nuspec file found inside the package.");
        }
        return nuspec;
    }

    @Override
    public Hash hash() {
        try {
            return new Hash(Hashing.sha512().hashBytes(this.content.read()));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
