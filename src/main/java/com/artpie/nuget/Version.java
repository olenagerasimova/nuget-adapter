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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version of package.
 * See <a href="https://docs.microsoft.com/en-us/nuget/concepts/package-versioning">Package versioning</a>.
 *
 * @since 0.1
 */
public final class Version {

    /**
     * RegEx pattern for matching version string.
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
        final Matcher matcher = this.matcher();
        final StringBuilder builder = new StringBuilder()
            .append(removeLeadingZeroes(matcher.group("major")))
            .append('.')
            .append(removeLeadingZeroes(matcher.group("minor")));
        final String patch = matcher.group("patch");
        if (patch != null) {
            builder.append('.').append(removeLeadingZeroes(patch));
        }
        final String revision = matcher.group("revision");
        if (revision != null) {
            final String rev = removeLeadingZeroes(revision);
            if (!rev.equals("0")) {
                builder.append('.').append(rev);
            }
        }
        final String label = matcher.group("label");
        if (label != null) {
            builder.append('-').append(label);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return this.raw;
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
