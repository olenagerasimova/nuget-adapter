/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.nuget.metadata.Nuspec;
import com.artipie.nuget.metadata.PackageId;
import com.artipie.nuget.metadata.Version;
import com.google.common.io.ByteSource;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Nupkg}.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class NupkgTest {

    /**
     * Resource `newtonsoft.json.12.0.3.nupkg` name.
     */
    private String name;

    @BeforeEach
    void init() {
        this.name = "newtonsoft.json.12.0.3.nupkg";
    }

    @Test
    void shouldCalculateHash() {
        final Storage storage = new InMemoryStorage();
        final PackageIdentity identity = new PackageIdentity(
            new PackageId("foo"),
            new Version("1.0.0")
        );
        new Nupkg(ByteSource.wrap("test data".getBytes(StandardCharsets.UTF_8)))
            .hash()
            .save(storage, identity)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            storage.value(identity.hashKey())
                .thenApply(PublisherAs::new)
                .thenCompose(PublisherAs::asciiString)
                .toCompletableFuture().join(),
            Matchers.equalTo(
                // @checkstyle LineLength (1 lines)
                "Dh4h7PEF7IU9JNcohnrXBhPCFmOkaTB0sqNhnBvTnWa1iMM3I7tGbHJCToDjymPCSQeKs0e6uUKFAOfuQwWdDQ=="
            )
        );
    }

    @Test
    void shouldExtractNuspec() throws Exception {
        final Nuspec nuspec = new Nupkg(
            ByteSource.wrap(new NewtonJsonResource(this.name).bytes())
        ).nuspec();
        MatcherAssert.assertThat(
            nuspec.id().normalized(),
            Matchers.is("newtonsoft.json")
        );
    }
}
