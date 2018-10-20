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
package org.springframework.statemachine.dsl.ssml.support;

import org.antlr.v4.runtime.Token;
import org.springframework.dsl.domain.Position;
import org.springframework.dsl.domain.Range;
import org.springframework.dsl.service.reconcile.ProblemSeverity;
import org.springframework.dsl.service.reconcile.ProblemType;
import org.springframework.dsl.service.reconcile.ReconcileProblem;

/**
 * A {@link ReconcileProblem} related to missing transition source reference.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlTransitionSourceStateDslParserResultError implements ReconcileProblem {

	private final Token token;

	public SsmlTransitionSourceStateDslParserResultError(Token token) {
		this.token = token;
	}

	@Override
	public ProblemType getType() {
		return PROBLEM;
	}

	@Override
	public String getMessage() {
		return "undefined state '" + token.getText() + "' referenced in transition source";
	}

	@Override
	public Range getRange() {
		Position start = new Position(token.getLine() - 1, token.getCharPositionInLine());
		Position end = new Position(token.getLine() - 1, token.getCharPositionInLine());
		return new Range(start, end);
	}

	private static ProblemType PROBLEM = new ProblemType() {

		@Override
		public ProblemSeverity getSeverity() {
			return ProblemSeverity.ERROR;
		}

		@Override
		public String getCode() {
			return "code";
		}
	};
}
