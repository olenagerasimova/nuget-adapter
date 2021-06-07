/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PackageId}.
 *
 * @since 0.1
 */
public class PackageIdTest {

    @Test
    void shouldPreserveOriginal() {
        final String id = "Microsoft.Extensions.Logging";
        MatcherAssert.assertThat(
            new PackageId(id).original(),
            Matchers.is(id)
        );
    }

    @Test
    void shouldGenerateRootKey() {
        MatcherAssert.assertThat(
            new PackageId("Artipie.Module").rootKey().string(),
            new IsEqual<>("artipie.module")
        );
    }

    @Test
    void shouldGenerateVersionsKey() {
        MatcherAssert.assertThat(
            new PackageId("Newtonsoft.Json").versionsKey().string(),
            Matchers.is("newtonsoft.json/index.json")
        );
    }

    @Test
    void shouldGenerateLower() {
        MatcherAssert.assertThat(
            new PackageId("My.Lib").lower(),
            Matchers.is("my.lib")
        );
    }
}
