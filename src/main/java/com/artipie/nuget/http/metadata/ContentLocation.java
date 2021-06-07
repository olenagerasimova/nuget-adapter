/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget.http.metadata;

import com.artipie.nuget.PackageIdentity;
import java.net.URL;

/**
 * Package content location.
 *
 * @since 0.1
 */
public interface ContentLocation {

    /**
     * Get URL for package content.
     *
     * @param identity Package identity.
     * @return URL for package content.
     */
    URL url(PackageIdentity identity);
}
