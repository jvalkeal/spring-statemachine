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
package org.springframework.statemachine.dsl.ssmlserver;

import java.util.ArrayList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.dsl.antlr.AbstractAntlrLinter;
import org.springframework.dsl.antlr.AntlrFactory;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.reconcile.Linter;
import org.springframework.dsl.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlErrorListener;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.statemachine.dsl.ssml.SsmlStateMachineVisitor;

import reactor.core.publisher.Flux;

/**
 * A {@link Linter} for a {@code ssml} language.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlLinter extends AbstractAntlrLinter<SsmlLexer, SsmlParser> {

	private StateMachineComponentResolver<String, String> resolver;

	public SsmlLinter(AntlrFactory<SsmlLexer, SsmlParser> antlrFactory) {
		super(antlrFactory);
	}

	@Override
	public Flux<ReconcileProblem> lintInternal(Document document) {

		String content = document.get();

		CharStream antlrInputStream = CharStreams.fromString(content);

		SsmlLexer lexer = getAntlrFactory().createLexer(antlrInputStream);

		CommonTokenStream tokenStream = new CommonTokenStream(lexer);

		SsmlParser parser = getAntlrFactory().createParser(tokenStream);


		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		parser.removeErrorListeners();

		ArrayList<ReconcileProblem> errors = new ArrayList<>();
		parser.addErrorListener(new SsmlErrorListener(errors));
		ParseTree tree = parser.definitions();
		SsmlStateMachineVisitor<String, String> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors, resolver);
		StateMachineModel<String, String> model = stateMachineVisitor.visit(tree);

		return Flux.fromIterable(errors);
	}
}
