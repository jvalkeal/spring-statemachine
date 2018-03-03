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

import org.antlr.v4.runtime.Token;
import org.springframework.statemachine.dsl.DslParserResultError;

/**
 * A {@link DslParserResultError} related to missing transition source reference.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlTransitionSourceStateDslParserResultError implements DslParserResultError {

	private final Token token;

	public SsmlTransitionSourceStateDslParserResultError(Token token) {
		this.token = token;
	}

	@Override
	public int getLine() {
		return token.getLine();
	}

	@Override
	public int getPosition() {
		return token.getCharPositionInLine();
	}

	@Override
	public String getMessage() {
		return "undefined state '" + token.getText() + "' referenced in transition source";
	}
}
