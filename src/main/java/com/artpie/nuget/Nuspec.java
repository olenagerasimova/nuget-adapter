/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artpie.nuget;

import com.artipie.asto.blocking.BlockingStorage;
import com.google.common.io.ByteSource;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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
     * @throws IOException In case exception occurred on reading document.
     */
    public PackageIdentity identity() throws IOException {
        final XML xml = this.xml()
            .registerNs("ns", "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd");
        final String id = single(xml, "/ns:package/ns:metadata/ns:id/text()");
        final String version = single(xml, "/ns:package/ns:metadata/ns:version/text()");
        return new PackageIdentity(id, version);
    }

    /**
     * Saves .nuspec document to storage.
     *
     * @param storage Storage to use for saving.
     * @throws IOException In case exception occurred on reading document or writing it to storage.
     */
    public void save(final BlockingStorage storage) throws IOException {
        storage.save(this.identity().nuspecKey(), this.content.read());
    }

    /**
     * Parse binary content as XML document.
     *
     * @return Content as XML document.
     * @throws IOException In case exception occurred on reading document.
     */
    private XML xml() throws IOException {
        return new XMLDocument(new ByteArrayInputStream(this.content.read()));
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
