/*
 * Copyright the authors
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.length;
        result = prime * result + this.start;
        result = prime * result + ((this.text == null) ? 0 : this.text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Replacement other = (Replacement) obj;
        if (this.length != other.length)
            return false;
        if (this.start != other.start)
            return false;
        if (this.text == null) {
            if (other.text != null)
                return false;
        } else if (!this.text.equals(other.text))
            return false;
        return true;
    }


}
