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

import com.artpie.nuget.http.Absent;
import com.artpie.nuget.http.Resource;
import com.artpie.nuget.http.Route;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Package metadata route.
 * See <a href="https://docs.microsoft.com/en-us/nuget/api/registration-base-url-resource">Package Metadata</a>
 *
 * @since 0.1
 */
public final class PackageMetadata implements Route {

    /**
     * Base path for the route.
     */
    private static final String BASE = "/registrations";

    /**
     * RegEx pattern for registration path.
     */
    private static final Pattern REGISTRATION = Pattern.compile(
        String.format("%s/(?<id>[^/]+)/index.json$", PackageMetadata.BASE)
    );

    @Override
    public String path() {
        return PackageMetadata.BASE;
    }

    @Override
    public Resource resource(final String path) {
        final Matcher matcher = REGISTRATION.matcher(path);
        final Resource resource;
        if (matcher.find()) {
            resource = new Registration();
        } else {
            resource = new Absent();
        }
        return resource;
    }
}
