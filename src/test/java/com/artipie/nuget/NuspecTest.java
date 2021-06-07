/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.google.common.io.ByteSource;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Nuspec}.
 *
 * @since 0.1
 * @checkstyle LineLengthCheck (500 lines)
 */
class NuspecTest {

    /**
     * Resource `newtonsoft.json.nuspec` name.
     */
    private String name;

    /**
     * Nuspec created from resource.
     */
    private Nuspec nuspec;

    @BeforeEach
    void init() throws Exception {
        this.name = "newtonsoft.json.nuspec";
        this.nuspec = new Nuspec(ByteSource.wrap(new NewtonJsonResource(this.name).bytes()));
    }

    @Test
    void shouldExtractSpecFromDifPackage() {
        final Nuspec spec = new Nuspec(
            ByteSource.wrap(
                String.join(
                    "\n",
                    "\uFEFF<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                    "<package xmlns=\"http://schemas.microsoft.com/packaging/2013/01/nuspec.xsd\">",
                    "  <metadata minClientVersion=\"3.3.0\">",
                    "    <id>SampleForDeployment</id>",
                    "    <version>1.0.0</version>",
                    "    <authors>aripie</authors>",
                    "    <owners>aripie</owners>",
                    "    <requireLicenseAcceptance>false</requireLicenseAcceptance>",
                    "    <description>Sample for a deployment to Artipie</description>",
                    "    <tags>sample artipie</tags>",
                    "    <contentFiles>",
                    "      <files include=\"data.txt\" buildAction=\"Content\" />",
                    "    </contentFiles>",
                    "  </metadata>",
                    "</package>"
                ).getBytes(StandardCharsets.UTF_8)
            )
        );
        MatcherAssert.assertThat(
            spec.packageId().toString(),
            Matchers.equalTo("SampleForDeployment")
        );
        MatcherAssert.assertThat(
            spec.version().toString(),
            Matchers.equalTo("1.0.0")
        );
    }

    @Test
    void shouldExtractPackageId() {
        MatcherAssert.assertThat(
            this.nuspec.packageId().lower(),
            Matchers.equalTo("newtonsoft.json")
        );
    }

    @Test
    void shouldExtractVersion() {
        final Version version = this.nuspec.version();
        MatcherAssert.assertThat(
            version.normalized(),
            Matchers.equalTo("12.0.3")
        );
    }

    @Test
    void shouldExtractIdentity() {
        final PackageIdentity identity = this.nuspec.identity();
        MatcherAssert.assertThat(
            identity.nuspecKey().string(),
            Matchers.equalTo("newtonsoft.json/12.0.3/newtonsoft.json.nuspec")
        );
    }

    @Test
    void shouldSave() throws Exception {
        final Storage storage = new InMemoryStorage();
        this.nuspec.save(storage).toCompletableFuture().join();
        MatcherAssert.assertThat(
            storage.value(this.nuspec.identity().nuspecKey())
                .thenApply(PublisherAs::new)
                .thenCompose(PublisherAs::bytes)
                .toCompletableFuture().join(),
            Matchers.equalTo(new NewtonJsonResource(this.name).bytes())
        );
    }
}
