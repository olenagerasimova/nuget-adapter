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

import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithStatus;
import com.artpie.nuget.http.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import org.reactivestreams.Publisher;

/**
 * Registration resource.
 * See <a href="https://docs.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-pages-and-leaves">Registration pages and leaves</a>
 *
 * @since 0.1
 */
class Registration implements Resource {

    @Override
    public Response get() {
        final JsonObject json = Json.createObjectBuilder()
            .add("count", 0)
            .add("items", Json.createArrayBuilder())
            .build();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            JsonWriter writer = Json.createWriter(out)) {
            writer.writeObject(json);
            out.flush();
            return new RsWithStatus(
                new RsWithBody(ByteBuffer.wrap(out.toByteArray())),
                RsStatus.OK
            );
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to serialize JSON to bytes", ex);
        }
    }

    @Override
    public Response put(final Publisher<ByteBuffer> body) {
        return new RsWithStatus(RsStatus.METHOD_NOT_ALLOWED);
    }
}
