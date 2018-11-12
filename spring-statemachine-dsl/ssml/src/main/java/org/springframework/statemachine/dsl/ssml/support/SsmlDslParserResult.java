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

import java.util.List;

import org.springframework.dsl.DslParserResult;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineModel;

/**
 * A generic {@link DslParserResult} acting as a simple pass through for given
 * {@link StateMachineModel} and {@link ReconcileProblem}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class SsmlDslParserResult<S, E> implements DslParserResult<StateMachineModel<S, E>> {

	private final StateMachineModel<S, E> model;
	private final List<ReconcileProblem> errors;

	public SsmlDslParserResult(StateMachineModel<S, E> model, List<ReconcileProblem> errors) {
		this.model = model;
		this.errors = errors;
	}

	@Override
	public StateMachineModel<S, E> getResult() {
		return model;
	}

	@Override
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	@Override
	public List<ReconcileProblem> getErrors() {
		return errors;
	}
}
