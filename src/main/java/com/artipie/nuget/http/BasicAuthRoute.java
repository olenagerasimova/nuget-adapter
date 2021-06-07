/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget.http;

import com.artipie.http.auth.Identities;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.SliceAuth;

/**
 * Route supporting basic authentication.
 *
 * @since 0.2
 */
final class BasicAuthRoute implements Route {

    /**
     * Origin route.
     */
    private final Route origin;

    /**
     * Authorization.
     */
    private final Permission perm;

    /**
     * Authentication.
     */
    private final Identities ids;

    /**
     * Ctor.
     *
     * @param origin Origin route.
     * @param perm Authorization mechanism.
     * @param ids Authentication mechanism.
     */
    BasicAuthRoute(final Route origin, final Permission perm, final Identities ids) {
        this.origin = origin;
        this.ids = ids;
        this.perm = perm;
    }

    @Override
    public String path() {
        return this.origin.path();
    }

    @Override
    public Resource resource(final String path) {
        return new ResourceFromSlice(
            path,
            new SliceAuth(
                new SliceFromResource(this.origin.resource(path)),
                this.perm,
                this.ids
            )
        );
    }
}
