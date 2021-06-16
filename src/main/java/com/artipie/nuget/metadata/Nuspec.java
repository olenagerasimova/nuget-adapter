/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget.metadata;

import com.artipie.asto.ArtipieIOException;
import com.artipie.nuget.PackageId;
import com.google.common.io.ByteSource;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Package description in .nuspec format.
 * @since 0.6
 */
public interface Nuspec {

    /**
     * Package identifier: original case sensitive and lowercase.
     * @return Package id
     * @checkstyle MethodNameCheck (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    PackageId id();

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

    /**
     * Nuspec file bytes.
     * @return Bytes
     */
    byte[] bytes();

    /**
     * Implementation of {@link Nuspec}, reads metadata from byte source.
     *
     * @since 0.6
     */
    final class FromBytes implements Nuspec {

        /**
         * Binary content in .nuspec format.
         */
        private final ByteSource content;

        /**
         * Ctor.
         *
         * @param content Binary content of in .nuspec format.
         */
        public FromBytes(final ByteSource content) {
            this.content = content;
        }

        @Override
        @SuppressWarnings("PMD.ShortMethodName")
        public PackageId id() {
            return new PackageId(
                single(
                    this.xml(), "/*[name()='package']/*[name()='metadata']/*[name()='id']/text()"
                )
            );
        }

        @Override
        public NuspecField version() {
            final String version = single(
                this.xml(),
                "/*[name()='package']/*[name()='metadata']/*[name()='version']/text()"
            );
            return new Version(version);
        }

        @Override
        public String description() {
            return single(
                this.xml(),
                "/*[name()='package']/*[name()='metadata']/*[name()='description']/text()"
            );
        }

        @Override
        public String authors() {
            return single(
                this.xml(),
                "/*[name()='package']/*[name()='metadata']/*[name()='authors']/text()"
            );
        }

        @Override
        public byte[] bytes() {
            try {
                return this.content.read();
            } catch (final IOException ex) {
                throw new ArtipieIOException(ex);
            }
        }

        @Override
        public String toString() {
            return new String(this.bytes(), StandardCharsets.UTF_8);
        }

        /**
         * Parse binary content as XML document.
         *
         * @return Content as XML document.
         */
        private XML xml() {
            try {
                return new XMLDocument(new ByteArrayInputStream(this.bytes()));
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
}
