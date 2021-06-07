/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Content;
import com.artipie.asto.Storage;
import com.google.common.io.ByteSource;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Package description in .nuspec format.
 *
 * @since 0.1
 */
public final class Nuspec {

    /**
     * Binary content in .nuspec format.
     */
    private final ByteSource content;

    /**
     * Ctor.
     *
     * @param content Binary content of in .nuspec format.
     */
    public Nuspec(final ByteSource content) {
        this.content = content;
    }

    /**
     * Extract package identity from document.
     *
     * @return Package identity.
     */
    public PackageIdentity identity() {
        return new PackageIdentity(this.packageId(), this.version());
    }

    /**
     * Extract package identifier from document.
     *
     * @return Package identifier.
     */
    public PackageId packageId() {
        return new PackageId(
            single(this.xml(), "/*[name()='package']/*[name()='metadata']/*[name()='id']/text()")
        );
    }

    /**
     * Extract version from document.
     *
     * @return Package version.
     */
    public Version version() {
        final String version = single(
            this.xml(),
            "/*[name()='package']/*[name()='metadata']/*[name()='version']/text()"
        );
        return new Version(version);
    }

    /**
     * Saves .nuspec document to storage.
     *
     * @param storage Storage to use for saving.
     * @return Completion of save operation.
     */
    public CompletionStage<Void> save(final Storage storage) {
        try {
            return storage.save(this.identity().nuspecKey(), new Content.From(this.content.read()));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Parse binary content as XML document.
     *
     * @return Content as XML document.
     */
    private XML xml() {
        try {
            return new XMLDocument(new ByteArrayInputStream(this.content.read()));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Reads single string value from XML via XPath.
     * Exception is thrown if zero or more then 1 values found
     *
     * @param xml XML document to read from.
     * @param xpath XPath expression to select data from the XML.
     * @return Value found by XPath
     */
    private static String single(final XML xml, final String xpath) {
        final List<String> values = xml.xpath(xpath);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("No values found in path: '%s'", xpath)
            );
        }
        if (values.size() > 1) {
            throw new IllegalArgumentException(
                String.format("Multiple values found in path: '%s'", xpath)
            );
        }
        return values.get(0);
    }
}
