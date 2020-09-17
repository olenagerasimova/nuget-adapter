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
package com.artipie.nuget.http;

import com.artipie.asto.Storage;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicIdentities;
import com.artipie.http.auth.Identities;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.Permissions;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.nuget.Repository;
import com.artipie.nuget.http.content.PackageContent;
import com.artipie.nuget.http.index.ServiceIndex;
import com.artipie.nuget.http.metadata.PackageMetadata;
import com.artipie.nuget.http.publish.PackagePublish;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * NuGet repository HTTP front end.
 *
 * @since 0.1
 * @todo #84:30min Refactor NuGet class, reduce number of fields.
 *  There are too many fields and constructor parameters as result in this class.
 *  Probably it is needed to extract some additional abstractions to reduce it,
 *  joint Permissions and Identities might be one of them.
 * @checkstyle ParameterNumberCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class NuGet implements Slice {

    /**
     * Read permission name.
     */
    public static final String READ = "read";

    /**
     * Write permission name.
     */
    public static final String WRITE = "write";

    /**
     * Base URL.
     */
    private final URL url;

    /**
     * Storage for packages.
     */
    private final Storage storage;

    /**
     * Access permissions.
     */
    private final Permissions perms;

    /**
     * User identities.
     */
    private final Identities users;

    /**
     * Ctor.
     *
     * @param url Base URL.
     * @param storage Storage for packages.
     */
    public NuGet(final URL url, final Storage storage) {
        this(url, storage, Permissions.FREE, Identities.ANONYMOUS);
    }

    /**
     * Ctor.
     *
     * @param url Base URL.
     * @param storage Storage for packages.
     * @param perms Access permissions.
     * @param auth Auth details.
     */
    public NuGet(
        final URL url,
        final Storage storage,
        final Permissions perms,
        final Authentication auth
    ) {
        this(url, storage, perms, new BasicIdentities(auth));
    }

    /**
     * Ctor.
     *  @param url Base URL.
     * @param storage Storage for packages.
     * @param perms Access permissions.
     * @param users User identities.
     */
    private NuGet(
        final URL url,
        final Storage storage,
        final Permissions perms,
        final Identities users
    ) {
        this.url = url;
        this.storage = storage;
        this.perms = perms;
        this.users = users;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        final Response response;
        final RequestLineFrom request = new RequestLineFrom(line);
        final String path = request.uri().getPath();
        final Resource resource = this.resource(path);
        final RqMethod method = request.method();
        if (method.equals(RqMethod.GET)) {
            response = resource.get(new Headers.From(headers));
        } else if (method.equals(RqMethod.PUT)) {
            response = resource.put(new Headers.From(headers), body);
        } else {
            response = new RsWithStatus(RsStatus.METHOD_NOT_ALLOWED);
        }
        return response;
    }

    /**
     * Find resource by relative path.
     *
     * @param path Relative path.
     * @return Resource found by path.
     */
    private Resource resource(final String path) {
        final PackagePublish publish = new PackagePublish(this.storage);
        final PackageContent content = new PackageContent(this.url, this.storage);
        final PackageMetadata metadata = new PackageMetadata(
            new Repository(this.storage),
            content
        );
        return new RoutingResource(
            path,
            new ServiceIndex(
                Arrays.asList(
                    new RouteService(this.url, publish, "PackagePublish/2.0.0"),
                    new RouteService(this.url, metadata, "RegistrationsBaseUrl/Versioned"),
                    new RouteService(this.url, content, "PackageBaseAddress/3.0.0")
                )
            ),
            this.auth(publish, NuGet.WRITE),
            this.auth(content, NuGet.READ),
            this.auth(metadata, NuGet.READ)
        );
    }

    /**
     * Create route supporting basic authentication.
     *
     * @param route Route requiring authentication.
     * @param permission Permission name.
     * @return Authenticated route.
     */
    private Route auth(final Route route, final String permission) {
        return new BasicAuthRoute(
            route,
            new Permission.ByName(permission, this.perms),
            this.users
        );
    }
}
