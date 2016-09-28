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

package com.palantir.tslint.failure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 *
 *
 * @author alpapad
 */
public class Replacement {

    private final int start;
    private final int length;
    private final String text;

    public Replacement(@JsonProperty("innerStart") int innerStart, @JsonProperty("innerLength") int innerLength, @JsonProperty("innerText") String innerText) {
        super();
        this.start = innerStart;
        this.length = innerLength;
        this.text = innerText;
    }

    public int getStart() {
        return this.start;
    }

    public int getLength() {
        return this.length;
    }

    public String getText() {
        return this.text;
    }

    public String apply(String content) {
        return content.substring(0, this.start) + this.text + content.substring(this.start + this.length);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("start", this.start)
            .add("length", this.length)
            .add("text", this.text)
            .toString();
    }
}
