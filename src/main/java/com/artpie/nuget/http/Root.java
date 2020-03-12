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

package com.artpie.nuget.http;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artpie.nuget.InvalidPackageException;
import com.artpie.nuget.PackageVersionAlreadyExistsException;
import com.artpie.nuget.Repository;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.reactivestreams.Publisher;

/**
 * Root resource. Used as endpoint to push a package.
 * See <a href="https://docs.microsoft.com/en-us/nuget/api/package-publish-resource#push-a-package">Push a package</a>
 *
 * @since 0.1
 */
public final class Root implements Resource {

    /**
     * Storage to read content from.
     */
    private final Storage storage;

    /**
     * Ctor.
     *
     * @param storage Storage to read content from.
     */
    public Root(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public Response get() {
        return new RsWithStatus(RsStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public Response put(final Publisher<ByteBuffer> body) {
        return connection -> CompletableFuture
            .supplyAsync(() -> new Key.From(UUID.randomUUID().toString()))
            .thenCompose(
                key -> this.storage.save(key, body).thenCompose(
                    ignored -> {
                        RsStatus status;
                        try {
                            new Repository(
                                new BlockingStorage(this.storage)
                            ).add(key);
                            status = RsStatus.CREATED;
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        } catch (final InvalidPackageException ex) {
                            status = RsStatus.BAD_REQUEST;
                        } catch (final PackageVersionAlreadyExistsException ex) {
                            status = RsStatus.CONFLICT;
                        }
                        return new RsWithStatus(status).send(connection);
                    }
                )
            );
    }
}
