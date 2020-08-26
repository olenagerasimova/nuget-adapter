/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artipie.nuget;

import com.artipie.asto.blocking.BlockingStorage;
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
 * @checkstyle StringLiteralsConcatenationCheck (500 lines)
 * @checkstyle OperatorWrapCheck (500 lines)
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

    /**
     * A test for schema independence.
     * @throws Exception If fails
     * @checkstyle RegexpSingleline (500 lines)
     * @checkstyle RegexpSinglelineCheck (500 lines)
     */
    @Test
    void shouldExtractPackageIdFromDifPackage() throws Exception {
        MatcherAssert.assertThat(
            new Nuspec(
                ByteSource.wrap(
                    ("\uFEFF<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                        + "\n<package xmlns=\"http://schemas.microsoft.com/packaging/2013/01/nuspec.xsd\">"
                        + "\n  <metadata minClientVersion=\"3.3.0\">"
                        + "\n    <id>SampleForDeployment</id>"
                        + "\n    <version>1.0.0</version>"
                        + "\n    <authors>aripie</authors>"
                        + "\n    <owners>aripie</owners>"
                        + "\n    <requireLicenseAcceptance>false</requireLicenseAcceptance>"
                        + "\n    <description>Sample for a deployment to Artipie</description>"
                        + "\n    <tags>sample artipie</tags>"
                        + "\n    <contentFiles>"
                        + "\n      <files include=\"data.txt\" buildAction=\"Content\" />"
                        + "\n    </contentFiles>"
                        + "\n  </metadata>"
                        + "\n</package>"
                    ).getBytes(StandardCharsets.UTF_8)
                )
            ).packageId().toString(),
            Matchers.equalTo("SampleForDeployment")
        );
    }

    @Test
    void shouldExtractPackageId() throws Exception {
        MatcherAssert.assertThat(
            this.nuspec.packageId().lower(),
            Matchers.equalTo("newtonsoft.json")
        );
    }

    @Test
    void shouldExtractVersion() throws Exception {
        final Version version = this.nuspec.version();
        MatcherAssert.assertThat(
            version.normalized(),
            Matchers.equalTo("12.0.3")
        );
    }

    @Test
    void shouldExtractIdentity() throws Exception {
        final PackageIdentity identity = this.nuspec.identity();
        MatcherAssert.assertThat(
            identity.nuspecKey().string(),
            Matchers.equalTo("newtonsoft.json/12.0.3/newtonsoft.json.nuspec")
        );
    }

    @Test
    void shouldSave() throws Exception {
        final BlockingStorage storage = new BlockingStorage(new InMemoryStorage());
        this.nuspec.save(storage);
        MatcherAssert.assertThat(
            storage.value(this.nuspec.identity().nuspecKey()),
            Matchers.equalTo(new NewtonJsonResource(this.name).bytes())
        );
    }
}
