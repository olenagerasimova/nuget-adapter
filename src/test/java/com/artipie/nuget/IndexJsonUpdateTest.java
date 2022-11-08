/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import com.artipie.asto.test.TestResource;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Test for {@link IndexJson.Update}.
 * @since 1.5
 */
class IndexJsonUpdateTest {

    @Test
    void createsIndexJson() throws JSONException {
        JSONAssert.assertEquals(
            new String(
                new TestResource("IndexJsonUpdateTest/index_newtonsoft.json").asBytes(),
                StandardCharsets.UTF_8
            ),
            new IndexJson.Update().perform(
                new Nupkg(
                    new TestResource("IndexJsonUpdateTest/newtonsoft.json.12.0.3.nupkg")
                        .asInputStream()
                )
            ).toString(),
            true
        );
    }
}
