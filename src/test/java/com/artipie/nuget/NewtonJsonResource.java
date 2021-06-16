/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Content;
import com.google.common.io.ByteStreams;
import org.cactoos.io.ResourceOf;

/**
 * Newton.Json package resource.
 *
 * @since 0.1
 */
public final class NewtonJsonResource {

    /**
     * Resource name.
     */
    private final String name;

    /**
     * Ctor.
     *
     * @param name Resource name.
     */
    public NewtonJsonResource(final String name) {
        this.name = name;
    }

    /**
     * Reads binary data.
     *
     * @return Binary data.
     * @throws Exception In case exception occurred on reading resource content.
     */
    public Content content() throws Exception {
        return new Content.From(this.bytes());
    }

    /**
     * Reads binary data.
     *
     * @return Binary data.
     * @throws Exception In case exception occurred on reading resource content.
     */
    public byte[] bytes() throws Exception {
        return ByteStreams.toByteArray(
            new ResourceOf(String.format("newtonsoft.json/12.0.3/%s", this.name)).stream()
        );
    }
}
