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
package org.springframework.statemachine.dsl.ssml.antlr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dsl.antlr.AntlrParseResult;
import org.springframework.dsl.domain.DocumentSymbol;
import org.springframework.dsl.domain.SymbolInformation;
import org.springframework.dsl.domain.SymbolKind;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.dsl.service.symbol.SymbolizeInfo;
import org.springframework.dsl.symboltable.Scope;
import org.springframework.dsl.symboltable.SymbolTable;
import org.springframework.dsl.symboltable.support.DocumentSymbolTableVisitor;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.dsl.ssml.SsmlParser.DefinitionsContext;
import org.springframework.statemachine.dsl.ssml.support.SsmlSymbolTable;
import org.springframework.statemachine.guard.Guard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@code Visitor} visiting top level state machine definition.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class SsmlStateMachineVisitor<S, E> extends AbstractSsmlBaseVisitor<S, E, AntlrParseResult<StateMachineModel<S, E>>> {

	private final List<ReconcileProblem> errors;
	private final StateMachineComponentResolver<S, E> resolver;
	private final String uri;

	/**
	 * Instantiates a new ssml state machine visitor.
	 *
	 * @param errors the errors
	 * @param resolver the resolver
	 */
	public SsmlStateMachineVisitor(List<ReconcileProblem> errors, StateMachineComponentResolver<S, E> resolver) {
		this(errors, resolver, null);
	}

	/**
	 * Instantiates a new ssml state machine visitor.
	 *
	 * @param errors the errors
	 * @param resolver the resolver
	 * @param uri the uri
	 */
	public SsmlStateMachineVisitor(List<ReconcileProblem> errors, StateMachineComponentResolver<S, E> resolver, String uri) {
		super(resolver, new SsmlSymbolTable(), null);
		this.errors = errors;
		this.resolver = resolver;
		this.uri = uri;
	}

	@Override
	public AntlrParseResult<StateMachineModel<S, E>> visitDefinitions(DefinitionsContext ctx) {
		// TODO: visit machine as well as everything else outside of machine
		//       is kinda anonymous

		Scope scope = getSymbolTable().getGlobalScope();

		SsmlActionVisitor<S, E> actionVisitor = new SsmlActionVisitor<>(resolver, getSymbolTable(), scope);
		Map<String, Action<S, E>> actions = ctx.machineObjectList().action().stream()
				.map(actionContext -> actionContext.accept(actionVisitor))
				.collect(Collectors.toMap(result -> result.id, result -> result.action));

		SsmlGuardVisitor<S, E> guardVisitor = new SsmlGuardVisitor<>(resolver, getSymbolTable(), scope);
		Map<String, Guard<S, E>> guards = ctx.machineObjectList().guard().stream()
				.map(guardContext -> guardContext.accept(guardVisitor))
				.collect(Collectors.toMap(result -> result.id, result -> result.guard));

		SsmlStateVisitor<S, E> stateVisitor = new SsmlStateVisitor<>(resolver, actions, getSymbolTable(), scope);
		stateVisitor.setStateMapperFunction(getStateMapperFunction());
		stateVisitor.setEventMapperFunction(getEventMapperFunction());

		SsmlTransitionVisitor<S, E> transitionVisitor = new SsmlTransitionVisitor<>(resolver, errors, stateVisitor,
				guards, actions, getSymbolTable(), scope);
		transitionVisitor.setStateMapperFunction(getStateMapperFunction());
		transitionVisitor.setEventMapperFunction(getEventMapperFunction());

		List<StateData<S, E>> stateDatas = ctx.machineObjectList().state().stream()
			.map(stateContext -> stateContext.accept(stateVisitor))
			.collect(Collectors.toList());

		List<TransitionData<S, E>> transitionDatas = ctx.machineObjectList().transition().stream()
				.map(stateContext -> stateContext.accept(transitionVisitor))
				.collect(Collectors.toList());

		DefaultStateMachineModel<S, E> stateMachineModel = new DefaultStateMachineModel<S, E>(null,
				new StatesData<>(stateDatas), new TransitionsData<>(transitionDatas));

		// TODO: don't like below as it doesn't feel 'reactive', though
		//       as of now we know that callers will defer, etc.
		return new AntlrParseResult<StateMachineModel<S, E>>() {

			@Override
			public Mono<StateMachineModel<S,E>> getResult() {
				return Mono.just(stateMachineModel);
			};

			@Override
			public Mono<SymbolTable> getSymbolTable() {
				return Mono.just(SsmlStateMachineVisitor.this.getSymbolTable());
			};

			@Override
			public Flux<ReconcileProblem> getReconcileProblems() {
				return Flux.fromIterable(errors);
			}

			@Override
			public SymbolizeInfo getSymbolizeInfo() {
				Flux<DocumentSymbol> documentSymbols = getSymbolTable()
					.map(st -> {
						DocumentSymbolTableVisitor visitor = new DocumentSymbolTableVisitor(uri);
						st.visitSymbolTable(visitor);
						return visitor.getSymbolizeInfo();
					})
					.flatMapMany(si -> si.documentSymbols());

				Flux<SymbolInformation> symbolInformations = getSymbolTable()
						.map(st -> {
							DocumentSymbolTableVisitor visitor = new DocumentSymbolTableVisitor(uri);
							st.visitSymbolTable(visitor);
							return visitor.getSymbolizeInfo();
						})
						.flatMapMany(si -> si.symbolInformations());

				return SymbolizeInfo.of(documentSymbols, symbolInformations);
			}
		};
	}
}