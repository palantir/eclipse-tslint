package com.palantir.tslint.failure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RuleFailurePosition {
	private int character;
	private int line;
	private int position;

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
}
