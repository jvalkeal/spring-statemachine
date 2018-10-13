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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.dsl.antlr.AntlrCompletionEngine;
import org.springframework.dsl.antlr.AntlrCompletionResult;
import org.springframework.dsl.antlr.AntlrFactory;
import org.springframework.dsl.antlr.AntlrParseResult;
import org.springframework.dsl.antlr.support.AbstractAntlrParseResultFunction;
import org.springframework.dsl.antlr.support.DefaultAntlrCompletionEngine;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.domain.CompletionItem;
import org.springframework.dsl.domain.DocumentSymbol;
import org.springframework.dsl.domain.Position;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.dsl.symboltable.ClassSymbol;
import org.springframework.dsl.symboltable.DefaultSymbolTable;
import org.springframework.dsl.symboltable.SymbolTable;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlErrorListener;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlStateMachineVisitor;
import org.springframework.statemachine.guard.Guard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link Function} implementation for parsing results.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlAntlrParseResultFunction
		extends AbstractAntlrParseResultFunction<StateMachineModel<String, String>, SsmlLexer, SsmlParser> {

	private final StateMachineComponentResolver<String, String> resolver = new InternalStateMachineComponentResolver();

	/**
	 * Instantiates a new ssml antlr parse result function.
	 *
	 * @param antlrFactory the antlr factory
	 */
	public SsmlAntlrParseResultFunction(AntlrFactory<SsmlLexer, SsmlParser> antlrFactory) {
		super(antlrFactory);
	}

	@Override
	public Mono<? extends AntlrParseResult<StateMachineModel<String, String>>> apply(Document document) {

        return Mono.just(new AntlrParseResult<StateMachineModel<String, String>>() {

        	private Mono<SsmlDslParserResult> shared = parseResult(document, resolver).cache();

        	@Override
        	public Mono<StateMachineModel<String, String>> getResult() {
        		return shared.map(r -> r.getResult());
        	}

        	@Override
        	public Flux<ReconcileProblem> getReconcileProblems() {
        		return shared.map(r -> r.getErrors()).flatMapIterable(l -> l);
        	}

        	@Override
        	public Flux<CompletionItem> getCompletionItems(Position position) {
				Flux<String> items = Flux.defer(() -> {
	        		SsmlParser parser = getParser(CharStreams.fromString(document.content()));
			        AntlrCompletionEngine completionEngine = new DefaultAntlrCompletionEngine(parser, null, null);
					AntlrCompletionResult completionResult = completionEngine.collectResults(position,
							parser.definitions());
					ArrayList<String> completions = new ArrayList<String>();
					for (Entry<Integer, List<Integer>> e : completionResult.getTokens().entrySet()) {
						if (e.getKey() > 0) {
							Vocabulary vocabulary = parser.getVocabulary();
							String displayName = vocabulary.getDisplayName(e.getKey());
							completions.add(displayName);
						}
					}
					return Flux.fromIterable(completions);
				});
				return Flux.concat(items)
						.flatMap(c -> {
							return Mono.just(CompletionItem.completionItem().label(c).build());
						});
        	}

			@Override
			public Flux<DocumentSymbol> getDocumentSymbols() {
				return Flux.defer(() -> {
					return parseSymbolTable(document, resolver)
						.flatMapMany(st -> Flux.fromIterable(st.getAllSymbols()))
						.map(s -> DocumentSymbol.documentSymbol().name(s.getName()).build());
				});
			}
		});
	}

	private Mono<SymbolTable> parseSymbolTable(Document document, StateMachineComponentResolver<String, String> resolver) {
		return Mono.defer(() -> {
			ArrayList<ReconcileProblem> errors = new ArrayList<>();
			SsmlParser parser = getParser(CharStreams.fromString(document.content()));
			parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
			parser.removeErrorListeners();
			parser.addErrorListener(new SsmlErrorListener(errors));
			ParseTree tree = parser.definitions();
			SsmlStateMachineVisitor<String, String> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors, resolver);
			return stateMachineVisitor.visit(tree).getSymbolTable();
		});
	}

	private Mono<SsmlDslParserResult> parseResult(Document document, StateMachineComponentResolver<String, String> resolver) {
		return Mono.defer(() -> {
			ArrayList<ReconcileProblem> errors = new ArrayList<>();
			SsmlParser parser = getParser(CharStreams.fromString(document.content()));
			parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
			parser.removeErrorListeners();
			parser.addErrorListener(new SsmlErrorListener(errors));
			ParseTree tree = parser.definitions();
			SsmlStateMachineVisitor<String, String> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors, resolver);
			StateMachineModel<String, String> model = stateMachineVisitor.visit(tree).getResult().block();
			return Mono.just(new SsmlDslParserResult(model, errors));
		});
	}

	/**
	 * Resolver which never fails as needed for parsing to work without having real
	 * actions and guards available.
	 */
	private static class InternalStateMachineComponentResolver implements StateMachineComponentResolver<String, String> {

		@Override
		public Action<String, String> resolveAction(String id) {
			return (ctx) -> {};
		}

		@Override
		public Guard<String, String> resolveGuard(String id) {
			return (ctx) -> false;
		}
	}
}
