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
package com.artpie.nuget.http.metadata;

import com.artpie.nuget.Nuspec;
import com.artpie.nuget.PackageId;
import com.artpie.nuget.PackageIdentity;
import com.artpie.nuget.Repository;
import com.artpie.nuget.Version;
import java.io.IOException;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/**
 * Registration page.
 * See <a href="https://docs.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-page-object">Registration page</a>
 *
 * @since 0.1
 */
final class RegistrationPage {

    /**
     * Repository.
     */
    private final Repository repository;

    /**
     * Package content storage.
     */
    private final ContentStorage content;

    /**
     * Package identifier.
     */
    private final PackageId id;

    /**
     * Ordered list of versions on this page from lowest to highest.
     */
    private final List<Version> versions;

    /**
     * Ctor.
     *
     * @param repository Repository.
     * @param content Package content storage.
     * @param id Package identifier.
     * @param versions Ordered list of versions on this page from lowest to highest.
     * @todo #87:60min Refactor RegistrationPage class, reduce number of fields.
     *  Probably it is needed to extract some abstraction for creating leaf objects,
     *  that will join `repository` and `content` fields and produce leaf JSON for package identity.
     * @checkstyle ParameterNumberCheck (2 line)
     */
    RegistrationPage(
        final Repository repository,
        final ContentStorage content,
        final PackageId id,
        final List<Version> versions
    ) {
        this.repository = repository;
        this.content = content;
        this.id = id;
        this.versions = versions;
    }

    /**
     * Generates page in JSON.
     *
     * @return Page JSON.
     * @throws IOException In case exception occurred on reading data from repository.
     */
    public JsonObject json() throws IOException {
        if (this.versions.isEmpty()) {
            throw new IllegalStateException(
                String.format("Registration page contains no versions: '%s'", this.id)
            );
        }
        final Version lower = this.versions.get(0);
        final Version upper = this.versions.get(this.versions.size() - 1);
        final JsonArrayBuilder items = Json.createArrayBuilder();
        for (final Version version : this.versions) {
            items.add(this.leaf(new PackageIdentity(this.id, version)));
        }
        return Json.createObjectBuilder()
            .add("lower", lower.normalized())
            .add("upper", upper.normalized())
            .add("count", this.versions.size())
            .add("items", items)
            .build();
    }

    /**
     * Builds registration leaf.
     * See <a href="https://docs.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-leaf-object-in-a-page"></a>
     *
     * @param identity Package identity.
     * @return JSON representing registration leaf.
     * @throws IOException In case exception occurred on reading data from repository.
     */
    private JsonObject leaf(final PackageIdentity identity) throws IOException {
        final Nuspec nuspec = this.repository.nuspec(identity);
        return Json.createObjectBuilder()
            .add(
                "catalogEntry",
                Json.createObjectBuilder()
                    .add("id", nuspec.packageId().original())
                    .add("version", nuspec.version().normalized())
            )
            .add("packageContent", this.content.url(identity).toString())
            .build();
    }
}
