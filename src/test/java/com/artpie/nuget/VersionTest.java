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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link Version}.
 *
 * @since 0.1
 */
class VersionTest {

    @ParameterizedTest
    @CsvSource({
        "1.00,1.0",
        "1.01.1,1.1.1",
        "1.00.0.1,1.0.0.1",
        "1.0.0.0,1.0.0",
        "1.0.01.0,1.0.1",
        "0.0.4,0.0.4",
        "1.2.3,1.2.3",
        "10.20.30,10.20.30",
        "1.1.2-prerelease+meta,1.1.2-prerelease",
        "1.1.2+meta,1.1.2",
        "1.1.2+meta-valid,1.1.2",
        "1.0.0-alpha,1.0.0-alpha",
        "1.0.0-beta,1.0.0-beta",
        "1.0.0-alpha.beta,1.0.0-alpha.beta",
        "1.0.0-alpha.beta.1,1.0.0-alpha.beta.1",
        "1.0.0-alpha.1,1.0.0-alpha.1",
        "1.0.0-alpha0.valid,1.0.0-alpha0.valid",
        "1.0.0-alpha.0valid,1.0.0-alpha.0valid",
        "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay,1.0.0-alpha-a.b-c-somethinglong",
        "1.0.0-rc.1+build.1,1.0.0-rc.1",
        "2.0.0-rc.1+build.123,2.0.0-rc.1",
        "1.2.3-beta,1.2.3-beta",
        "10.2.3-DEV-SNAPSHOT,10.2.3-DEV-SNAPSHOT",
        "1.2.3-SNAPSHOT-123,1.2.3-SNAPSHOT-123",
        "1.0.0,1.0.0",
        "2.0.0,2.0.0",
        "1.1.7,1.1.7",
        "2.0.0+build.1848,2.0.0",
        "2.0.1-alpha.1227,2.0.1-alpha.1227",
        "1.0.0-alpha+beta,1.0.0-alpha",
        "1.2.3----RC-SNAPSHOT.12.9.1--.12+788,1.2.3----RC-SNAPSHOT.12.9.1--.12",
        "1.2.3----R-S.12.9.1--.12+meta,1.2.3----R-S.12.9.1--.12",
        "1.2.3----RC-SNAPSHOT.12.9.1--.12,1.2.3----RC-SNAPSHOT.12.9.1--.12",
        "1.0.0+0.build.1-rc.10000aaa-kk-0.1,1.0.0",
        //@checkstyle LineLengthCheck (1 line)
        "99999999999999999999999.999999999999999999.99999999999999999,99999999999999999999999.999999999999999999.99999999999999999",
        "1.0.0-0A.is.legal,1.0.0-0A.is.legal"
    })
    void shouldNormalize(final String original, final String expected) {
        final Version version = new Version(original);
        MatcherAssert.assertThat(
            version.normalized(),
            Matchers.equalTo(expected)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1",
        "1.1.2+.123",
        "+invalid",
        "-invalid",
        "-invalid+invalid",
        "-invalid.01",
        "alpha",
        "alpha.beta",
        "alpha.beta.1",
        "alpha.1",
        "alpha+beta",
        "alpha_beta",
        "alpha.",
        "alpha..",
        "beta",
        "1.0.0-alpha_beta",
        "-alpha.",
        "1.0.0-alpha..",
        "1.0.0-alpha..1",
        "1.0.0-alpha...1",
        "1.0.0-alpha....1",
        "1.0.0-alpha.....1",
        "1.0.0-alpha......1",
        "1.0.0-alpha.......1",
        "1.2.3.DEV",
        "1.2.31.2.3----RC-SNAPSHOT.12.09.1--..12+788",
        "+justmeta",
        "9.8.7+meta+meta",
        "9.8.7-whatever+meta+meta",
        //@checkstyle LineLengthCheck (1 line)
        "99999999999999999999999.999999999999999999.99999999999999999----RC-SNAPSHOT.12.09.1--------------------------------..12"
    })
    void shouldNotNormalize(final String original) {
        final Version version = new Version(original);
        Assertions.assertThrows(RuntimeException.class, version::normalized);
    }

    @ParameterizedTest
    @MethodSource("ordered")
    void shouldCompare(
        final List<String> lesser,
        final String target,
        final List<String> greater
    ) {
        final Version version = new Version(target);
        MatcherAssert.assertThat(
            version,
            new AllOf<Version>(
                Stream.of(
                    Stream.of(Matchers.comparesEqualTo(version)),
                    lesser.stream().map(Version::new).map(Matchers::greaterThan),
                    greater.stream().map(Version::new).map(Matchers::lessThan)
                ).<Matcher<? super Version>>flatMap(self -> self).collect(Collectors.toList())
            )
        );
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidUsingHardCodedIP"})
    private static Stream<Object[]> ordered() {
        return Stream.of(
            new String[] {"0.1", "0.2", "0.11", "1.0", "2.0", "2.1", "18.0"},
            new String[] {"3.0", "3.0.1", "3.0.2", "3.0.10", "3.1"},
            new String[] {"4.0.1", "4.0.1.1", "4.0.1.2", "4.0.1.17", "4.0.2"}
        ).map(Arrays::asList).flatMap(
            ordered -> IntStream.range(0, ordered.size()).mapToObj(
                index -> {
                    final List<String> lesser = ordered.subList(0, index);
                    final String target = ordered.get(index);
                    final List<String> greater = ordered.subList(index + 1, ordered.size());
                    return new Object[] {lesser, target, greater};
                }
            )
        );
    }
}
