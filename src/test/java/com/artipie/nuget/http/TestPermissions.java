/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget.http;

import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.Permissions;

/**
 * Single user with single action allowed permissions for usage in tests.
 *
 * @since 0.2
 */
public final class TestPermissions implements Permissions {

    /**
     * User name.
     */
    private final String username;

    /**
     * Allowed action.
     */
    private final String action;

    /**
     * Ctor.
     *
     * @param username User name.
     * @param allowed Allowed action.
     */
    public TestPermissions(final String username, final String allowed) {
        this.username = username;
        this.action = allowed;
    }

    @Override
    public boolean allowed(final Authentication.User user, final String act) {
        return this.username.equals(user.name()) && this.action.equals(act);
    }

    /**
     * Single user with read allowed permissions for usage in tests.
     *
     * @since 0.3
     */
    public static class Read extends Permissions.Wrap {

        /**
         * Ctor.
         *
         * @param username User name.
         */
        public Read(final String username) {
            super(new TestPermissions(username, "read"));
        }
    }

    /**
     * Single user with write allowed permissions for usage in tests.
     *
     * @since 0.3
     */
    public static class Write extends Permissions.Wrap {

        /**
         * Ctor.
         *
         * @param username User name.
         */
        public Write(final String username) {
            super(new TestPermissions(username, "write"));
        }
    }
}
