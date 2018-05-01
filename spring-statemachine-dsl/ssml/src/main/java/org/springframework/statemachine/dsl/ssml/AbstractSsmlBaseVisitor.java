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

import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
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

	AbstractSsmlBaseVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver) {
		Assert.notNull(stateMachineComponentResolver, "stateMachineComponentResolver must be set");
		this.stateMachineComponentResolver = stateMachineComponentResolver;
	}

	StateMachineComponentResolver<S, E> getStateMachineComponentResolver() {
		return stateMachineComponentResolver;
	}
}
