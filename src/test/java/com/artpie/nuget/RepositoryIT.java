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

package com.artpie.nuget;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.jcabi.log.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

    @Test
    void shouldAddPackage() throws Exception {
        final Path repo = this.temp.resolve("repo");
        final BlockingStorage storage = new BlockingStorage(new FileStorage(repo));
        final Key.From source = new Key.From("package.zip");
        storage.save(source, new NewtonJsonResource("newtonsoft.json.12.0.3.nupkg").bytes());
        final Repository repository = new Repository(storage);
        repository.add(source);
        final Path stdout = this.temp.resolve("stdout.txt");
        final Path project = this.temp.resolve("project");
        Files.createDirectory(project);
        new ProcessBuilder()
            .directory(project.toFile())
            .command(
                "nuget", "install",
                "newtonsoft.json", "-Version", "12.0.3",
                "-NoCache",
                "-Source", repo.toString()
            )
            .redirectOutput(stdout.toFile())
            .redirectErrorStream(true)
            .start()
            .waitFor();
        final String log = new String(Files.readAllBytes(stdout));
        Logger.debug(this, "Full stdout/stderr:\n%s", log);
        MatcherAssert.assertThat(
            log,
            Matchers.containsString("Successfully installed 'newtonsoft.json 12.0.3'")
        );
    }
}
