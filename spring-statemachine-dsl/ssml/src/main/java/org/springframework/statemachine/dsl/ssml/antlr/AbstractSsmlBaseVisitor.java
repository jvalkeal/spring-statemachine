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

import java.util.function.Function;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.springframework.dsl.symboltable.Scope;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.dsl.ssml.SsmlParserBaseVisitor;
import org.springframework.statemachine.dsl.ssml.support.SsmlSymbolTable;
import org.springframework.util.Assert;

/**
 * Base implementation of a {@link ParseTreeVisitor} providing shared functionality.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <T> the type of visitor
 */
abstract class AbstractSsmlBaseVisitor<S, E, T> extends SsmlParserBaseVisitor<T> {

	private final StateMachineComponentResolver<S, E> stateMachineComponentResolver;
	private final SsmlSymbolTable symbolTable;
	private final Scope scope;

	@SuppressWarnings("unchecked")
	private Function<String, S> stateMapperFunction = id -> (S)id;
	@SuppressWarnings("unchecked")
	private Function<String, E> eventMapperFunction = id -> (E)id;

	AbstractSsmlBaseVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			SsmlSymbolTable symbolTable, Scope scope) {
		Assert.notNull(stateMachineComponentResolver, "stateMachineComponentResolver must be set");
		Assert.notNull(symbolTable, "symbolTable must be set");
		this.stateMachineComponentResolver = stateMachineComponentResolver;
		this.symbolTable = symbolTable;
		this.scope = scope;
	}

	/**
	 * Gets the state machine component resolver.
	 *
	 * @return the state machine component resolver
	 */
	public StateMachineComponentResolver<S, E> getStateMachineComponentResolver() {
		return stateMachineComponentResolver;
	}

	/**
	 * Gets the symbol table.
	 *
	 * @return the symbol table
	 */
	public SsmlSymbolTable getSymbolTable() {
		return symbolTable;
	}

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Gets the state mapper function.
	 *
	 * @return the state mapper function
	 */
	public Function<String, S> getStateMapperFunction() {
		return stateMapperFunction;
	}

	/**
	 * Sets the state mapper function.
	 *
	 * @param stateMapperFunction the state mapper function
	 */
	public void setStateMapperFunction(Function<String, S> stateMapperFunction) {
		this.stateMapperFunction = stateMapperFunction;
	}

	/**
	 * Gets the event mapper function.
	 *
	 * @return the event mapper function
	 */
	public Function<String, E> getEventMapperFunction() {
		return eventMapperFunction;
	}

	/**
	 * Sets the event mapper function.
	 *
	 * @param eventMapperFunction the event mapper function
	 */
	public void setEventMapperFunction(Function<String, E> eventMapperFunction) {
		this.eventMapperFunction = eventMapperFunction;
	}
}
