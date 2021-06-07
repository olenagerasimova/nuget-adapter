/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Storage;
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
 * @since 0.5
 */
class AstoRepositoryIT {

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
        final Repository repository = new AstoRepository(storage);
        repository.add(
            new NewtonJsonResource("newtonsoft.json.12.0.3.nupkg").content()
        ).toCompletableFuture().join();
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
