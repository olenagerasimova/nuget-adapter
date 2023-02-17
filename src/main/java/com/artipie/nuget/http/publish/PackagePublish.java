/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */

package com.artipie.nuget.http.publish;

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.nuget.InvalidPackageException;
import com.artipie.nuget.PackageVersionAlreadyExistsException;
import com.artipie.nuget.Repository;
import com.artipie.nuget.http.Resource;
import com.artipie.nuget.http.Route;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import org.reactivestreams.Publisher;

/**
 * Package publish service, used to pushing new packages and deleting existing ones.
 * See <a href="https://docs.microsoft.com/en-us/nuget/api/package-publish-resource">Push and Delete</a>
 *
 * @since 0.1
 */
public final class PackagePublish implements Route {

    /**
     * Repository for adding package.
     */
    private final Repository repository;

    /**
     * Ctor.
     *
     * @param repository Repository for adding package.
     */
    public PackagePublish(final Repository repository) {
        this.repository = repository;
    }

    @Override
    public String path() {
        return "/package";
    }

    @Override
    public Resource resource(final String path) {
        return new NewPackage(this.repository);
    }

    /**
     * New package resource. Used to push a package into repository.
     * See <a href="https://docs.microsoft.com/en-us/nuget/api/package-publish-resource#push-a-package">Push a package</a>
     *
     * @since 0.1
     */
    public static final class NewPackage implements Resource {

        /**
         * Repository for adding package.
         */
        private final Repository repository;

        /**
         * Ctor.
         *
         * @param repository Repository for adding package.
         */
        public NewPackage(final Repository repository) {
            this.repository = repository;
        }

        @Override
        public Response get(final Headers headers) {
            return new RsWithStatus(RsStatus.METHOD_NOT_ALLOWED);
        }

        @Override
        public Response put(
            final Headers headers,
            final Publisher<ByteBuffer> body
        ) {
            return new AsyncResponse(
                CompletableFuture.supplyAsync(
                    () -> new Multipart(headers, body).first()
                ).thenCompose(
                    content -> this.repository.add(content).thenApply(
                        nothing -> RsStatus.CREATED
                    ).exceptionally(
                        throwable -> toStatus(throwable.getCause())
                    ).thenApply(RsWithStatus::new)
                )
            );
        }

        /**
         * Converts throwable to HTTP response status.
         *
         * @param throwable Throwable.
         * @return HTTP response status.
         */
        private static RsStatus toStatus(final Throwable throwable) {
            final RsStatus status;
            if (throwable instanceof InvalidPackageException) {
                status = RsStatus.BAD_REQUEST;
            } else if (throwable instanceof PackageVersionAlreadyExistsException) {
                status = RsStatus.CONFLICT;
            } else {
                status = RsStatus.INTERNAL_ERROR;
            }
            return status;
        }
    }
}
