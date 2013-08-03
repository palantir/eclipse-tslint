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

public final class RuleFailure {
	private String failure;
	private String name;
	private RuleFailurePosition startPosition;
	private RuleFailurePosition endPosition;

	public RuleFailure(@JsonProperty("failure") String failure,
			@JsonProperty("name") String name,
			@JsonProperty("startPosition") RuleFailurePosition startPosition,
			@JsonProperty("endPosition") RuleFailurePosition endPosition) {
		this.failure = failure;
		this.name = name;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	public String getFailure() {
		return this.failure;
	}

	public String getName() {
		return this.name;
	}

	public RuleFailurePosition getStartPosition() {
		return this.startPosition;
	}

	public RuleFailurePosition getEndPosition() {
		return this.endPosition;
	}
}
