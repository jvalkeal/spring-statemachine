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
import org.springframework.dsl.lsp.domain.Position;
import org.springframework.dsl.lsp.domain.Range;
import org.springframework.dsl.reconcile.ProblemSeverity;
import org.springframework.dsl.reconcile.ProblemType;
import org.springframework.dsl.reconcile.ReconcileProblem;

/**
 * A {@code DslParserResultError} related to missing transition target reference.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlTransitionTargetStateDslParserResultError implements ReconcileProblem {

	private final Token token;

	public SsmlTransitionTargetStateDslParserResultError(Token token) {
		this.token = token;
	}

	@Override
	public ProblemType getType() {
		return PROBLEM;
	}

	@Override
	public String getMessage() {
		return "undefined state '" + token.getText() + "' referenced in transition target";
	}

	@Override
	public Range getRange() {
		Position start = new Position(token.getLine(), token.getCharPositionInLine());
		Position end = new Position(token.getLine(), token.getCharPositionInLine());
		return new Range(start, end);
	}

	@Override
	public String getCode() {
		return "xxx";
	}

	private static ProblemType PROBLEM = new ProblemType() {

		@Override
		public ProblemSeverity getDefaultSeverity() {
			return ProblemSeverity.ERROR;
		}

		@Override
		public String getCode() {
			return "code";
		}
	};
}
