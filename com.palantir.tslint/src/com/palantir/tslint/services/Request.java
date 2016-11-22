/*
 * Copyright 2013 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.tslint.services;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * Represents a request which will be sent to the bridge.
 *
 * @author aramaswamy
 */
public final class Request {

    @JsonProperty("method")
    private final String method;

    @JsonProperty("arguments")
    private final List<Object> arguments;

    public Request(String method, Object... arguments) {
        checkNotNull(method);
        checkNotNull(arguments);

        this.method = method;
        this.arguments = Collections.unmodifiableList(Arrays.asList(arguments));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("method", this.method)
            .add("arguments", this.arguments)
            .toString();
    }
}
