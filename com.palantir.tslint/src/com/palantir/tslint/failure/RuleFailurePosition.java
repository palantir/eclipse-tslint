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

public final class RuleFailurePosition {
    private final int character;
    private final int line;
    private final int position;

    public RuleFailurePosition(@JsonProperty("character") int character,
            @JsonProperty("line") int line,
            @JsonProperty("position") int position) {
        this.character = character;
        this.line = line;
        this.position = position;
    }

    public int getCharacter() {
        return this.character;
    }

    public int getLine() {
        return this.line;
    }

    public int getPosition() {
        return this.position;
    }
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("line", this.line)
            .add("position", this.position)
            .add("character", this.character)
            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.character;
        result = prime * result + this.line;
        result = prime * result + this.position;
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
        RuleFailurePosition other = (RuleFailurePosition) obj;
        if (this.character != other.character)
            return false;
        if (this.line != other.line)
            return false;
        if (this.position != other.position)
            return false;
        return true;
    }


}
