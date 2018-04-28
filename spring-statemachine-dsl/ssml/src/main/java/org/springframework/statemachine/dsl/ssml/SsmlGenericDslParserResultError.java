/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.dsl.ssml;

import org.springframework.dsl.DslParserResultError;

/**
 * Generic {@link DslParserResultError} which is a simple pass through for
 * externally given message, line and position.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlGenericDslParserResultError implements DslParserResultError {

	private String message;
	private int line;
	private int position;

	public SsmlGenericDslParserResultError(String message, int line, int position) {
		this.message = message;
		this.line = line;
		this.position = position;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
