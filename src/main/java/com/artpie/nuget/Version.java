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

import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version of package.
 * See <a href="https://docs.microsoft.com/en-us/nuget/concepts/package-versioning">Package versioning</a>.
 * Comparison of version strings is implemented using SemVer 2.0.0's <a href="https://semver.org/spec/v2.0.0.html#spec-item-11">version precedence rules</a>.
 *
 * @todo `compareTo` method should respect labels comparison rules of SevVer spec
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Version implements Comparable<Version> {

    /**
     * RegEx pattern for matching version string.
     *
     * @checkstyle StringLiteralsConcatenationCheck (7 lines)
     */
    private static final Pattern PATTERN = Pattern.compile(
        String.join(
            "",
            "(?<major>\\d+)\\.(?<minor>\\d+)",
            "(\\.(?<patch>\\d+)(\\.(?<revision>\\d+))?)?",
            "(-(?<label>[0-9a-zA-Z\\-]+(\\.[0-9a-zA-Z\\-]+)*))?",
            "(\\+(?<metadata>[0-9a-zA-Z\\-]+(\\.[0-9a-zA-Z\\-]+)*))?",
            "$"
        )
    );

    /**
     * Raw version string.
     */
    private final String raw;

    /**
     * Ctor.
     *
     * @param raw Raw version string.
     */
    public Version(final String raw) {
        this.raw = raw;
    }

    /**
     * Get normalized version.
     * See <a href="https://docs.microsoft.com/en-us/nuget/concepts/package-versioning#normalized-version-numbers">Normalized version numbers</a>.
     *
     * @return Normalized version string.
     */
    public String normalized() {
        final StringBuilder builder = new StringBuilder()
            .append(removeLeadingZeroes(this.major()))
            .append('.')
            .append(removeLeadingZeroes(this.minor()));
        this.patch().ifPresent(
            patch -> builder.append('.').append(removeLeadingZeroes(patch))
        );
        this.revision().ifPresent(
            revision -> {
                final String rev = removeLeadingZeroes(revision);
                if (!rev.equals("0")) {
                    builder.append('.').append(rev);
                }
            }
        );
        this.group("label").ifPresent(
            label -> builder.append('-').append(label)
        );
        return builder.toString();
    }

    @Override
    public int compareTo(final Version that) {
        return Comparator
            .<Version>comparingInt(version -> Integer.parseInt(version.major()))
            .thenComparingInt(version -> Integer.parseInt(version.minor()))
            .thenComparingInt(version -> version.patch().map(Integer::parseInt).orElse(0))
            .thenComparingInt(version -> version.revision().map(Integer::parseInt).orElse(0))
            .compare(this, that);
    }

    @Override
    public String toString() {
        return this.raw;
    }

    /**
     * Major version.
     *
     * @return String representation of major version.
     */
    private String major() {
        return this.group("major").orElseThrow(
            () -> new IllegalStateException("Major identifier is missing")
        );
    }

    /**
     * Minor version.
     *
     * @return String representation of minor version.
     */
    private String minor() {
        return this.group("minor").orElseThrow(
            () -> new IllegalStateException("Minor identifier is missing")
        );
    }

    /**
     * Patch part of version.
     *
     * @return Patch part of version, none if absent.
     */
    private Optional<String> patch() {
        return this.group("patch");
    }

    /**
     * Revision part of version.
     *
     * @return Revision part of version, none if absent.
     */
    private Optional<String> revision() {
        return this.group("revision");
    }

    /**
     * Get named group from RegEx matcher.
     *
     * @param name Group name.
     * @return Group value, or nothing if absent.
     */
    private Optional<String> group(final String name) {
        return Optional.ofNullable(this.matcher().group(name));
    }

    /**
     * Get RegEx matcher by version pattern.
     *
     * @return Matcher by pattern.
     */
    private Matcher matcher() {
        final Matcher matcher = PATTERN.matcher(this.raw);
        if (!matcher.find()) {
            throw new IllegalStateException(
                String.format("Unexpected version format: %s", this.raw)
            );
        }
        return matcher;
    }

    /**
     * Removes leading zeroes from a string. Last zero is preserved.
     *
     * @param string Original string.
     * @return String without leading zeroes.
     */
    private static String removeLeadingZeroes(final String string) {
        return string.replaceFirst("^0+(?!$)", "");
    }
}
