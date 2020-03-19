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

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.google.common.io.ByteSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonWriter;

/**
 * NuGet package version enumeration.
 *
 * @since 0.1
 */
public final class Versions {

    /**
     * Name of array in JSON containing versions.
     */
    private static final String ARRAY = "versions";

    /**
     * Packages registry content.
     */
    private final ByteSource content;

    /**
     * Ctor.
     */
    public Versions() {
        this(
            bytes(
                Json.createObjectBuilder()
                    .add(Versions.ARRAY, Json.createArrayBuilder())
                    .build()
            )
        );
    }

    /**
     * Ctor.
     *
     * @param content Packages registry content.
     */
    public Versions(final ByteSource content) {
        this.content = content;
    }

    /**
     * Add version.
     *
     * @param version Version.
     * @return Updated versions.
     * @throws IOException In case of any I/O problems.
     */
    public Versions add(final Version version) throws IOException {
        final JsonObject json = this.json();
        final JsonArray versions = json.getJsonArray(Versions.ARRAY);
        final JsonArrayBuilder builder;
        if (versions == null) {
            builder = Json.createArrayBuilder();
        } else {
            builder = Json.createArrayBuilder(versions);
        }
        builder.add(version.normalized());
        return new Versions(
            bytes(
                Json.createObjectBuilder(json)
                    .add(Versions.ARRAY, builder)
                    .build()
            )
        );
    }

    /**
     * Read all package versions.
     *
     * @return All versions sorted by natural order.
     * @throws IOException In case of any I/O problems.
     */
    public List<Version> all() throws IOException {
        return this.json()
            .getJsonArray(Versions.ARRAY)
            .getValuesAs(JsonString.class)
            .stream()
            .map(JsonString::getString)
            .map(Version::new)
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Saves binary content to storage.
     *
     * @param storage Storage to use for saving.
     * @param key Key to store data at.
     * @throws IOException In case exception occurred on saving content.
     */
    public void save(final BlockingStorage storage, final Key key) throws IOException {
        storage.save(key, this.content.read());
    }

    /**
     * Reads content as JSON object.
     *
     * @return JSON object.
     * @throws IOException In case exception occurred on reading content.
     */
    private JsonObject json() throws IOException {
        try (JsonReader reader = Json.createReader(this.content.openStream())) {
            return reader.readObject();
        }
    }

    /**
     * Serializes JSON object into bytes.
     *
     * @param json JSON object.
     * @return Serialized JSON object.
     */
    private static ByteSource bytes(final JsonObject json) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            JsonWriter writer = Json.createWriter(out)) {
            writer.writeObject(json);
            out.flush();
            return ByteSource.wrap(out.toByteArray());
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to serialize JSON to bytes", ex);
        }
    }
}
