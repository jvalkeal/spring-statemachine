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
import java.util.function.Function;

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
import org.springframework.dsl.domain.Hover;
import org.springframework.dsl.domain.Position;
import org.springframework.dsl.domain.SymbolInformation;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.dsl.service.symbol.SymbolizeInfo;
import org.springframework.dsl.support.DslUtils;
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
		return Mono.defer(() -> {
			// need to shim everything else than completion items as it takes position, thus
			// we need to handle it in this level. further stuff is just delegated to a shared
			// and cached parsing result.

			// TODO: for above comment, need to think if getCompletionItems(Position position) in
			//       AntlrParseResult is realistically useless as when original parsing happens
			//       positional info is not available, thus one might think that function should
			//       not be in that interface. you see issues with it as shown below.
			Mono<AntlrParseResult<StateMachineModel<String, String>>> shared = parse(document, resolver).cache();
			return Mono.just(new AntlrParseResult<StateMachineModel<String, String>>() {

				@Override
				public Mono<StateMachineModel<String, String>> getResult() {
					return shared.flatMap(r -> Mono.from(r.getResult()));
				};

				@Override
				public Mono<SymbolTable> getSymbolTable() {
					return shared.flatMap(r -> Mono.from(r.getSymbolTable()));
				};

				@Override
				public Flux<ReconcileProblem> getReconcileProblems() {
					return shared.flatMapMany(r -> Flux.from(r.getReconcileProblems()));
				}

	        	@Override
	        	public Flux<CompletionItem> getCompletionItems(Position position) {
					Flux<String> items = Flux.defer(() -> {
		        		SsmlParser parser = getParser(CharStreams.fromString(document.content()));
				        AntlrCompletionEngine completionEngine = new DefaultAntlrCompletionEngine(parser, null, null);
						AntlrCompletionResult completionResult = completionEngine.collectResults(position,
								parser.definitions());
						ArrayList<String> completions = new ArrayList<String>();
						Vocabulary vocabulary = parser.getVocabulary();
						for (Entry<Integer, List<Integer>> e : completionResult.getTokens().entrySet()) {
							Integer key = e.getKey();
							if (key > 0) {
								if (key == SsmlParser.LBRACE) {
									completions.add("{");
								} else if (key == SsmlParser.RBRACE) {
									completions.add("}");
								} else if (key == SsmlParser.SEMI) {
									completions.add(";");
								} else if (key == SsmlParser.COMMA) {
									completions.add(",");
								} else {
									completions.add(vocabulary.getDisplayName(key));
								}
							}
						}
						return Flux.fromIterable(completions);
					});
					return Flux.concat(items)
							.flatMap(c -> {
								return Mono.just(CompletionItem.completionItem().label(c.toLowerCase()).build());
							});
	        	}

	        	@Override
	        	public SymbolizeInfo getSymbolizeInfo() {

	        		Mono<SymbolizeInfo> map = shared.map(r -> r.getSymbolizeInfo());
	        		Mono<Flux<DocumentSymbol>> map2 = map.map(si -> si.documentSymbols());
	        		Flux<DocumentSymbol> flatMapMany1 = map2.flatMapMany(x -> x);
	        		Mono<Flux<SymbolInformation>> map3 = map.map(si -> si.symbolInformations());
	        		Flux<SymbolInformation> flatMapMany2 = map3.flatMapMany(x -> x);

	        		SymbolizeInfo symbolizeInfo = SymbolizeInfo.of(flatMapMany1, flatMapMany2);
	        		return symbolizeInfo;
	        	}

	        	@Override
	        	public Mono<Hover> getHover(Position position) {

	        		SymbolizeInfo symbolizeInfo = getSymbolizeInfo();
	        		Flux<DocumentSymbol> documentSymbols = symbolizeInfo.documentSymbols();

	        		return documentSymbols
	        			.filter(s -> DslUtils.isPositionInRange(position, s.getRange()))
	        			.map(s -> Hover.hover()
	        				.contents()
	        					.value(s.getName())
	        					.and()
	        				.range(s.getRange())
	        				.build())
	        			.next();
	        	}
			});
		});
	}

	/**
	 * Parse a document and return a mono for parsing result.
	 *
	 * @param document the document
	 * @param resolver the resolver
	 * @return the mono of antlr parse result of a state machine model
	 */
	private Mono<AntlrParseResult<StateMachineModel<String, String>>> parse(Document document,
			StateMachineComponentResolver<String, String> resolver) {
		return Mono.defer(() -> {
			ArrayList<ReconcileProblem> errors = new ArrayList<>();
			SsmlParser parser = getParser(CharStreams.fromString(document.content()));
			parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
			parser.removeErrorListeners();
			parser.addErrorListener(new SsmlErrorListener(errors));
			ParseTree tree = parser.definitions();
			SsmlStateMachineVisitor<String, String> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors,
					resolver, document.uri());
			return Mono.just(stateMachineVisitor.visit(tree));
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
