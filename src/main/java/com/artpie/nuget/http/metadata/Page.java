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

import com.artpie.nuget.Version;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * Registrations page.
 *
 * @since 0.1
 */
final class Page {

    /**
     * Ordered list of versions on this page from lowest to highest.
     */
    private final List<Version> versions;

    /**
     * Ctor.
     *
     * @param versions Ordered list of versions on this page from lowest to highest.
     */
    Page(final List<Version> versions) {
        this.versions = versions;
    }

    /**
     * Generates page in JSON.
     *
     * @return Page JSON.
     */
    public JsonObject json() {
        final Version lower = this.versions.get(0);
        final Version upper = this.versions.get(this.versions.size() - 1);
        return Json.createObjectBuilder()
            .add("lower", lower.normalized())
            .add("upper", upper.normalized())
            .add("count", this.versions.size())
            .add("items", Json.createArrayBuilder())
            .build();
    }
}
