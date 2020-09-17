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

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.google.common.collect.ImmutableList;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration test for NuGet repository.
 *
 * @since 0.1
 */
class RepositoryIT {

    // @checkstyle VisibilityModifierCheck (5 lines)
    /**
     * Temporary directory.
     */
    @TempDir
    Path temp;

    /**
     * Path to NuGet repository directory.
     */
    private Path repo;

    @BeforeEach
    void setUp() {
        this.repo = this.temp.resolve("repo");
    }

    @Test
    void shouldInstallAddedPackage() throws Exception {
        this.addPackage();
        MatcherAssert.assertThat(
            run(
                "install",
                "newtonsoft.json", "-Version", "12.0.3",
                "-NoCache"
            ),
            Matchers.containsString("Successfully installed 'newtonsoft.json 12.0.3'")
        );
    }

    @Test
    void shouldListAddedPackage() throws Exception {
        this.addPackage();
        MatcherAssert.assertThat(
            run(
                "list",
                "Newtonsoft.Json",
                "-AllVersions"
            ),
            Matchers.containsString("Newtonsoft.Json 12.0.3")
        );
    }

    private void addPackage() throws Exception {
        final Storage storage = new FileStorage(this.repo);
        final Key.From source = new Key.From("package.zip");
        new BlockingStorage(storage).save(
            source,
            new NewtonJsonResource("newtonsoft.json.12.0.3.nupkg").bytes()
        );
        final Repository repository = new Repository(storage);
        repository.add(source);
    }

    private String run(final String... args) throws IOException, InterruptedException {
        final Path stdout = this.temp.resolve("stdout.txt");
        final Path project = this.temp.resolve("project");
        Files.createDirectory(project);
        new ProcessBuilder()
            .directory(project.toFile())
            .command(
                ImmutableList.<String>builder()
                    .add("nuget")
                    .add(args)
                    .add("-Source", this.repo.toString())
                    .build()
            )
            .redirectOutput(stdout.toFile())
            .redirectErrorStream(true)
            .start()
            .waitFor();
        final String log = new String(Files.readAllBytes(stdout));
        Logger.debug(this, "Full stdout/stderr:\n%s", log);
        return log;
    }
}
