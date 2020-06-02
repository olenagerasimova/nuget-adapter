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

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.auth.Permissions;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.nuget.http.NuGet;
import com.artipie.nuget.http.TestAuthentication;
import com.artipie.vertx.VertxSliceServer;
import com.google.common.collect.ImmutableList;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration test for NuGet repository.
 *
 * @since 0.1
 * @todo #84:30min Enable auth in tests on Linux.
 *  NuGet client hangs up on pushing a package when authentication is required.
 *  This prevents integration tests from running on Linux with auth enabled.
 *  There might be a workaround for this issue.
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class RepositoryHttpIT {

    // @checkstyle VisibilityModifierCheck (5 lines)
    /**
     * Temporary directory.
     */
    @TempDir
    Path temp;

    /**
     * HTTP server hosting NuGet repository.
     */
    private VertxSliceServer server;

    /**
     * Packages source name in config.
     */
    private String source;

    /**
     * NuGet config file path.
     */
    private Path config;

    @BeforeEach
    void setUp() throws Exception {
        final int port = 8080;
        final String path = String.format("/%s", UUID.randomUUID().toString());
        final String base = String.format("http://localhost:%s%s", port, path);
        this.server = new VertxSliceServer(
            new LoggingSlice(
                new NuGet(
                    new URL(base),
                    path,
                    new InMemoryStorage(),
                    this.permissions(),
                    new TestAuthentication()
                )
            ),
            port
        );
        this.server.start();
        this.source = "artipie-nuget-test";
        this.config = this.temp.resolve("NuGet.Config");
        Files.write(
            this.config,
            this.configXml(
                String.format("%s/index.json", base),
                TestAuthentication.USERNAME,
                TestAuthentication.PASSWORD
            )
        );
    }

    @AfterEach
    void tearDown() {
        if (this.server != null) {
            this.server.stop();
        }
    }

    @Test
    void shouldPushPackage() throws Exception {
        MatcherAssert.assertThat(
            this.pushPackage(),
            new StringContains(false, "Your package was pushed.")
        );
    }

    @Test
    void shouldInstallPushedPackage() throws Exception {
        this.pushPackage();
        MatcherAssert.assertThat(
            runNuGet(
                "install",
                "Newtonsoft.Json", "-Version", "12.0.3",
                "-NoCache"
            ),
            Matchers.containsString("Successfully installed 'Newtonsoft.Json 12.0.3'")
        );
    }

    private String pushPackage() throws Exception {
        final String file = UUID.randomUUID().toString();
        Files.write(
            this.temp.resolve(file),
            new NewtonJsonResource("newtonsoft.json.12.0.3.nupkg").bytes()
        );
        return runNuGet("push", file);
    }

    private byte[] configXml(final String url, final String user, final String pwd) {
        return String.join(
            "",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n",
            "<configuration>",
            "<packageSources>",
            String.format("<add key=\"%s\" value=\"%s\" />", this.source, url),
            "</packageSources>",
            "<packageSourceCredentials>",
            String.format("<%s>", this.source),
            String.format("<add key=\"Username\" value=\"%s\"/>", user),
            String.format("<add key=\"ClearTextPassword\" value=\"%s\"/>", pwd),
            String.format("</%s>", this.source),
            "</packageSourceCredentials>",
            "</configuration>"
        ).getBytes();
    }

    private String runNuGet(final String... args) throws IOException, InterruptedException {
        final Path stdout = this.temp.resolve(
            String.format("%s-stdout.txt", UUID.randomUUID().toString())
        );
        final int code = new ProcessBuilder()
            .directory(this.temp.toFile())
            .command(
                ImmutableList.<String>builder()
                    .add(RepositoryHttpIT.command())
                    .add(args)
                    .add("-ConfigFile", this.config.toString())
                    .add("-Source", this.source)
                    .add("-Verbosity", "detailed")
                    .build()
            )
            .redirectOutput(stdout.toFile())
            .redirectErrorStream(true)
            .start()
            .waitFor();
        final String log = new String(Files.readAllBytes(stdout));
        Logger.debug(this, "Full stdout/stderr:\n%s", log);
        if (code != 0) {
            throw new IllegalStateException(String.format("Not OK exit code: %d", code));
        }
        return log;
    }

    private static String command() {
        final String cmd;
        if (isWindows()) {
            cmd = "nuget.exe";
        } else {
            cmd = "nuget";
        }
        return cmd;
    }

    private Permissions permissions() {
        final Permissions permissions;
        if (isWindows()) {
            permissions = (name, action) -> TestAuthentication.USERNAME.equals(name);
        } else {
            permissions = Permissions.FREE;
        }
        return permissions;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
