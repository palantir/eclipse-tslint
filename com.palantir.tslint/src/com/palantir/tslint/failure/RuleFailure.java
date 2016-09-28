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
}
