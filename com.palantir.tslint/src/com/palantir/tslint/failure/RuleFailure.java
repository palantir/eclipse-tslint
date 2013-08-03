package com.palantir.tslint.failure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RuleFailure {
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
