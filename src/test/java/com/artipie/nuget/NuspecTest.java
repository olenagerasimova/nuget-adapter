/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.nuget.metadata.Nuspec;
import com.artipie.nuget.metadata.NuspecField;
import com.google.common.io.ByteSource;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Nuspec.FromBytes}.
 *
 * @since 0.1
 * @checkstyle LineLengthCheck (500 lines)
 */
class NuspecTest {

    /**
     * Nuspec created from resource.
     */
    private Nuspec.FromBytes nuspec;

    @BeforeEach
    void init() throws Exception {
        this.nuspec = new Nuspec.FromBytes(
            ByteSource.wrap(new NewtonJsonResource("newtonsoft.json.nuspec").bytes())
        );
    }

    @Test
    void shouldExtractSpecFromDifPackage() {
        final Nuspec.FromBytes spec = new Nuspec.FromBytes(
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
            spec.id().toString(),
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
            this.nuspec.id().normalized(),
            Matchers.equalTo("newtonsoft.json")
        );
    }

    @Test
    void shouldExtractVersion() {
        final NuspecField version = this.nuspec.version();
        MatcherAssert.assertThat(
            version.normalized(),
            Matchers.equalTo("12.0.3")
        );
    }

}
