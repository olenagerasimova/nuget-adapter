/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget.http;

import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicAuthSlice;
import com.artipie.http.auth.Permission;

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
    private final Authentication auth;

    /**
     * Ctor.
     *
     * @param origin Origin route.
     * @param perm Authorization mechanism.
     * @param auth Authentication mechanism.
     */
    BasicAuthRoute(final Route origin, final Permission perm, final Authentication auth) {
        this.origin = origin;
        this.auth = auth;
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
            new BasicAuthSlice(
                new SliceFromResource(this.origin.resource(path)),
                this.auth,
                this.perm
            )
        );
    }
}
