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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.core.io.Resource;
import org.springframework.dsl.DslException;
import org.springframework.dsl.DslParser;
import org.springframework.dsl.DslParserResult;
import org.springframework.dsl.antlr.support.AntlrObjectSupport;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.StateMachineDslParser;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlErrorListener;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlStateMachineVisitor;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * {@link DslParser} implementation parsing {@code SSML} language.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class SsmlDslParser<S, E> extends AntlrObjectSupport<SsmlLexer, SsmlParser>
		implements StateMachineDslParser<S, E, StateMachineModel<S, E>> {

	private StateMachineComponentResolver<S, E> resolver;
	@SuppressWarnings("unchecked")
	private Function<String, S> stateMapperFunction = id -> (S)id;
	@SuppressWarnings("unchecked")
	private Function<String, E> eventMapperFunction = id -> (E)id;

	/**
	 * Instantiates a new ssml dsl parser.
	 */
	public SsmlDslParser() {
		// TODO: think we should force StateMachineComponentResolver?
		super(SsmlLanguage.ANTRL_FACTORY);
	}

	/**
	 * Instantiates a new ssml dsl parser.
	 *
	 * @param resolver the resolver
	 */
	public SsmlDslParser(StateMachineComponentResolver<S, E> resolver) {
		super(SsmlLanguage.ANTRL_FACTORY);
		Assert.notNull(resolver, "StateMachineComponentResolver must be set");
		this.resolver = resolver;
	}

	@Override
	public DslParserResult<StateMachineModel<S, E>> parse(Resource resource) {
		String content = null;
		try {
			content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		} catch (IOException e) {
			throw new DslException(e);
		}
		ArrayList<ReconcileProblem> errors = new ArrayList<>();
		SsmlParser parser = getParser(CharStreams.fromString(content));

		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		parser.removeErrorListeners();
		parser.addErrorListener(new SsmlErrorListener(errors));

		ParseTree tree = parser.definitions();

		SsmlStateMachineVisitor<S, E> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors, resolver);
		stateMachineVisitor.setStateMapperFunction(stateMapperFunction);
		stateMachineVisitor.setEventMapperFunction(eventMapperFunction);

		StateMachineModel<S, E> model = stateMachineVisitor.visit(tree).getResult().block();

		return new SsmlDslParserResult<S, E>(model, errors);
	}

	@Override
	public void setStateMachineComponentResolver(StateMachineComponentResolver<S, E> resolver) {
		this.resolver = resolver;
	}

	/**
	 * Sets the state mapper function.
	 *
	 * @param stateMapperFunction the state mapper function
	 */
	public void setStateMapperFunction(Function<String, S> stateMapperFunction) {
		Assert.notNull(stateMapperFunction, "stateMapperFunction cannot be null");
		this.stateMapperFunction = stateMapperFunction;
	}

	/**
	 * Sets the event mapper function.
	 *
	 * @param eventMapperFunction the event mapper function
	 */
	public void setEventMapperFunction(Function<String, E> eventMapperFunction) {
		Assert.notNull(eventMapperFunction, "eventMapperFunction cannot be null");
		this.eventMapperFunction = eventMapperFunction;
	}
}
