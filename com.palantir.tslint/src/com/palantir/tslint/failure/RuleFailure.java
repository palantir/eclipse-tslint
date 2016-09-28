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

public final class RuleFailure {
	private final String failure;
	private final String name;
	private final String ruleName;
	private final RuleFailurePosition startPosition;
	private final RuleFailurePosition endPosition;
	private final Fix fix;

	//fix
	public RuleFailure(@JsonProperty("failure") String failure,
			@JsonProperty("name") String name,
			@JsonProperty("ruleName") String ruleName,
			@JsonProperty("startPosition") RuleFailurePosition startPosition,
			@JsonProperty("endPosition") RuleFailurePosition endPosition,
			@JsonProperty("fix") Fix fix
			) {
		this.failure = failure;
		this.name = name;
		this.ruleName = ruleName;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.fix = fix;
	}

	public String getFailure() {
		return this.failure;
	}

	public String getName() {
		return this.name;
	}

	public String getRuleName() {
        return this.ruleName;
    }

	public RuleFailurePosition getStartPosition() {
		return this.startPosition;
	}

	public RuleFailurePosition getEndPosition() {
		return this.endPosition;
	}

    public Fix getFix() {
        return this.fix;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("start", this.failure)
            .add("length", this.name)
            .add("ruleName", this.ruleName)
            .add("startPosition", this.startPosition)
            .add("endPosition", this.endPosition)
            .add("fix", this.fix)
            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endPosition == null) ? 0 : this.endPosition.hashCode());
        result = prime * result + ((this.failure == null) ? 0 : this.failure.hashCode());
        result = prime * result + ((this.fix == null) ? 0 : this.fix.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.ruleName == null) ? 0 : this.ruleName.hashCode());
        result = prime * result + ((this.startPosition == null) ? 0 : this.startPosition.hashCode());
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
        RuleFailure other = (RuleFailure) obj;
        if (this.endPosition == null) {
            if (other.endPosition != null)
                return false;
        } else if (!this.endPosition.equals(other.endPosition))
            return false;
        if (this.failure == null) {
            if (other.failure != null)
                return false;
        } else if (!this.failure.equals(other.failure))
            return false;
        if (this.fix == null) {
            if (other.fix != null)
                return false;
        } else if (!this.fix.equals(other.fix))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        if (this.ruleName == null) {
            if (other.ruleName != null)
                return false;
        } else if (!this.ruleName.equals(other.ruleName))
            return false;
        if (this.startPosition == null) {
            if (other.startPosition != null)
                return false;
        } else if (!this.startPosition.equals(other.startPosition))
            return false;
        return true;
    }



}
