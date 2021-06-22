/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget;

import com.artipie.asto.Storage;
import com.artipie.asto.fs.FileStorage;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

/**
 * Integration test for NuGet repository.
 *
 * @since 0.5
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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

    /**
     * Container.
     */
    private GenericContainer<?> cntn;

    @BeforeEach
    void setUp() {
        this.repo = this.temp.resolve("repo");
        this.cntn = new GenericContainer<>("centeredge/nuget")
            .withCommand("tail", "-f", "/dev/null")
            .withWorkingDirectory("/home/")
            .withFileSystemBind(this.temp.toString(), "/home");
        this.cntn.start();
    }

    @Test
    void shouldInstallAddedPackage() throws Exception {
        this.addPackage();
        MatcherAssert.assertThat(
            run(
                "nuget", "install", "newtonsoft.json", "-Version", "12.0.3", "-NoCache",
                "-Source", "/home/repo"
            ),
            Matchers.containsString("Successfully installed 'newtonsoft.json 12.0.3'")
        );
    }

    @Test
    void shouldListAddedPackage() throws Exception {
        this.addPackage();
        MatcherAssert.assertThat(
            run("nuget", "list", "Newtonsoft.Json", "-AllVersions", "-Source", "/home/repo"),
            Matchers.containsString("Newtonsoft.Json 12.0.3")
        );
    }

    @AfterEach
    void clear() throws IOException, InterruptedException {
        this.cntn.execInContainer("rm", "/home/*");
        this.cntn.stop();
    }

    private void addPackage() throws Exception {
        final Storage storage = new FileStorage(this.repo);
        final Repository repository = new AstoRepository(storage);
        repository.add(
            new NewtonJsonResource("newtonsoft.json.12.0.3.nupkg").content()
        ).toCompletableFuture().join();
    }

    private String run(final String... args) throws IOException, InterruptedException {
        final Container.ExecResult res = this.cntn.execInContainer(args);
        Logger.debug(this, "Full stdout/stderr:\n%s", res.toString());
        return res.toString();
    }
}
