/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

/**
 * Exception indicates that package is invalid and so cannot be handled by repository.
 *
 * @since 0.1
 */
@SuppressWarnings("serial")
public final class InvalidPackageException extends RuntimeException {
    /**
     * Ctor.
     *
     * @param cause Underlying cause for package being invalid.
     */
    public InvalidPackageException(final Throwable cause) {
        super(cause);
    }
}
