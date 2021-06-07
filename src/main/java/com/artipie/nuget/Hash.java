/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Content;
import com.artipie.asto.Storage;
import com.google.common.hash.HashCode;
import java.util.Base64;
import java.util.concurrent.CompletionStage;

/**
 * Package hash.
 *
 * @since 0.1
 */
public final class Hash {

    /**
     * Calculated hash code value.
     */
    private final HashCode value;

    /**
     * Ctor.
     *
     * @param value Calculated hash code value.
     */
    public Hash(final HashCode value) {
        this.value = value;
    }

    /**
     * Saves hash to storage as base64 string.
     *
     * @param storage Storage to use for saving.
     * @param identity Package identity.
     * @return Completion of save operation.
     */
    public CompletionStage<Void> save(final Storage storage, final PackageIdentity identity) {
        return storage.save(
            identity.hashKey(),
            new Content.From(Base64.getEncoder().encode(this.value.asBytes()))
        );
    }
}
