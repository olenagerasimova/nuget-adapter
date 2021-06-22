/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.ArtipieIOException;
import com.artipie.asto.Content;
import com.artipie.asto.Storage;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletionStage;

/**
 * Package hash.
 *
 * @since 0.1
 */
public final class Hash {

    /**
     * Bytes to calculate hash code value from.
     */
    private final ByteSource value;

    /**
     * Ctor.
     *
     * @param value Bytes to calculate hash code value from.
     */
    public Hash(final ByteSource value) {
        this.value = value;
    }

    /**
     * Saves hash to storage as base64 string.
     *
     * @param storage Storage to use for saving.
     * @param identity Package identity.
     * @return Completion of save operation.
     * @throws ArtipieIOException On error
     */
    public CompletionStage<Void> save(final Storage storage, final PackageIdentity identity) {
        try {
            return storage.save(
                identity.hashKey(),
                new Content.From(
                    Base64.getEncoder().encode(
                        Hashing.sha512().hashBytes(this.value.read()).asBytes()
                    )
                )
            );
        } catch (final IOException err) {
            throw new ArtipieIOException(err);
        }
    }
}
