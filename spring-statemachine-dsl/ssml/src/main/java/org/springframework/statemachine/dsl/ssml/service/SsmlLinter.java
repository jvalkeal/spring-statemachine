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
package org.springframework.statemachine.dsl.ssml.service;

import java.util.function.Function;

import org.springframework.dsl.antlr.AntlrParseResult;
import org.springframework.dsl.antlr.AntlrParseService;
import org.springframework.dsl.antlr.support.AbstractAntlrLinter;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.service.reconcile.Linter;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;

import reactor.core.publisher.Mono;

/**
 * A {@link Linter} for a {@code ssml} language.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlLinter extends AbstractAntlrLinter<StateMachineModel<String, String>> {

	public SsmlLinter(AntlrParseService<StateMachineModel<String, String>> antlrParseService,
			Function<Document, Mono<? extends AntlrParseResult<StateMachineModel<String, String>>>> antlrParseResultSupplier) {
		super(SsmlLanguage.LANGUAGE_ID, antlrParseService, antlrParseResultSupplier);
	}

//	private StateMachineComponentResolver<String, String> resolver;
//
//	public SsmlLinter(AntlrFactory<SsmlLexer, SsmlParser> antlrFactory) {
//		super(antlrFactory);
//	}

//	@Override
//	public Flux<ReconcileProblem> lintInternal(Document document) {
//
//		String content = document.content();
//
//		CharStream antlrInputStream = CharStreams.fromString(content);
//
//		SsmlLexer lexer = getAntlrFactory().createLexer(antlrInputStream);
//
//		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
//
//		SsmlParser parser = getAntlrFactory().createParser(tokenStream);
//
//
//		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
//		parser.removeErrorListeners();
//
//		ArrayList<ReconcileProblem> errors = new ArrayList<>();
//		parser.addErrorListener(new SsmlErrorListener(errors));
//		ParseTree tree = parser.definitions();
//		SsmlStateMachineVisitor<String, String> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors, resolver);
//		StateMachineModel<String, String> model = stateMachineVisitor.visit(tree);
//
//		return Flux.fromIterable(errors);
//	}
}
