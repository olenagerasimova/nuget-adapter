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
package com.artpie.nuget.http.metadata;

import com.artpie.nuget.Version;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Page}.
 *
 * @since 0.1
 */
class PageTest {

    @Test
    void shouldHaveLower() {
        final String lower = "0.1.1";
        final Page page = new Page(
            Stream.of(lower, "0.1.2", "0.1.3").map(Version::new).collect(Collectors.toList())
        );
        MatcherAssert.assertThat(
            page.json().getString("lower"),
            new IsEqual<>(lower)
        );
    }

    @Test
    void shouldHaveUpper() {
        final String upper = "0.2.3";
        final Page page = new Page(
            Stream.of("0.2.1", "0.2.2", upper).map(Version::new).collect(Collectors.toList())
        );
        MatcherAssert.assertThat(
            page.json().getString("upper"),
            new IsEqual<>(upper)
        );
    }

    @Test
    void shouldHaveCount() {
        final Page page = new Page(
            Stream.of("0.3.1", "0.3.2").map(Version::new).collect(Collectors.toList())
        );
        MatcherAssert.assertThat(
            page.json().getInt("count"),
            new IsEqual<>(2)
        );
    }

    @Test
    void shouldHaveItems() {
        final Page page = new Page(
            Stream.of("0.4.1", "0.4.2").map(Version::new).collect(Collectors.toList())
        );
        MatcherAssert.assertThat(
            page.json().getJsonArray("items"),
            new IsEmptyCollection<>()
        );
    }
}
