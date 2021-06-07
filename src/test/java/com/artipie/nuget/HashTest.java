/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.google.common.hash.HashCode;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Hash}.
 *
 * @since 0.1
 */
class HashTest {

    @Test
    void shouldSave() {
        final String id = "abc";
        final String version = "0.0.1";
        final Storage storage = new InMemoryStorage();
        new Hash(HashCode.fromString("0123456789abcdef")).save(
            storage,
            new PackageIdentity(new PackageId(id), new Version(version))
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            storage.value(new Key.From(id, version, "abc.0.0.1.nupkg.sha512"))
                .thenApply(PublisherAs::new)
                .thenCompose(PublisherAs::bytes)
                .toCompletableFuture().join(),
            Matchers.equalTo("ASNFZ4mrze8=".getBytes(StandardCharsets.US_ASCII))
        );
    }
}
