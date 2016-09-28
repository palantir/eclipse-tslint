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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * @author alpapad
 */
public class Fix {

    private final String ruleName;
    private final List<Replacement> replacements;

    public Fix(@JsonProperty("innerRuleName") String innerRuleName,
            @JsonProperty("innerReplacements") List<Replacement> innerReplacements) {
        super();
        this.ruleName = innerRuleName;
        this.replacements = innerReplacements;
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public List<Replacement> getReplacements() {
        return this.replacements;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ruleName", this.ruleName)
            .add("replacements", this.replacements)
            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.replacements == null) ? 0 : this.replacements.hashCode());
        result = prime * result + ((this.ruleName == null) ? 0 : this.ruleName.hashCode());
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
        Fix other = (Fix) obj;
        if (this.replacements == null) {
            if (other.replacements != null)
                return false;
        } else if (!this.replacements.equals(other.replacements))
            return false;
        if (this.ruleName == null) {
            if (other.ruleName != null)
                return false;
        } else if (!this.ruleName.equals(other.ruleName))
            return false;
        return true;
    }


}
