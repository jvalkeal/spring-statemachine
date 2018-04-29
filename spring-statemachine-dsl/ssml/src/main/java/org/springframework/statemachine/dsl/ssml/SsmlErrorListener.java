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

import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.springframework.dsl.lsp.domain.Position;
import org.springframework.dsl.lsp.domain.Range;
import org.springframework.dsl.lsp.server.result.method.annotation.DefaultReconcileProblem;
import org.springframework.dsl.reconcile.ProblemSeverity;
import org.springframework.dsl.reconcile.ProblemType;
import org.springframework.dsl.reconcile.ReconcileProblem;

/**
 * {@link ANTLRErrorListener} implementing {@code ssml} related error and warning handling.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlErrorListener extends BaseErrorListener {

	private final List<ReconcileProblem> errors;

	public SsmlErrorListener(List<ReconcileProblem> errors) {
		this.errors = errors;
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		Position start = new Position(line, charPositionInLine);
		Position end = new Position(line, charPositionInLine);
		errors.add(new DefaultReconcileProblem(PROBLEM, msg, new Range(start, end), "xxx"));
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