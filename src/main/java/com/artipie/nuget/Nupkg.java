/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import com.artipie.asto.ArtipieIOException;
import com.artipie.nuget.metadata.Nuspec;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Package in .nupkg format.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Nupkg implements NuGetPackage {

    /**
     * Binary content of package.
     */
    private final InputStream content;

    /**
     * Ctor.
     *
     * @param content Binary content of package.
     */
    public Nupkg(final ByteSource content) {
        this(Nupkg.fromByteSource(content));
    }

    /**
     * Ctor.
     *
     * @param content Binary content of package.
     */
    public Nupkg(final InputStream content) {
        this.content = content;
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Nuspec nuspec() {
        Nuspec nuspec = null;
        try (ZipInputStream zipStream = new ZipInputStream(this.content)) {
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
                    nuspec = new Nuspec.Xml(zipStream);
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

    /**
     * Open input stream from ByteSource.
     * @param source Source
     * @return Input stream
     * @throws ArtipieIOException On IO error
     */
    private static InputStream fromByteSource(final ByteSource source) {
        try {
            return source.openStream();
        } catch (final IOException err) {
            throw new ArtipieIOException(err);
        }
    }
}
