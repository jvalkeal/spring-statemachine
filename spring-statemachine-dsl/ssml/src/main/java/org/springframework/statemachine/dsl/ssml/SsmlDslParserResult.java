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

import org.springframework.dsl.DslParserResult;
import org.springframework.dsl.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineModel;

/**
 * A generic {@link DslParserResult} acting as a simple pass through for given
 * {@link StateMachineModel} and {@link ReconcileProblem}s.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlDslParserResult implements DslParserResult<StateMachineModel<String, String>> {

	private final StateMachineModel<String, String> model;
	private final List<ReconcileProblem> errors;

	public SsmlDslParserResult(StateMachineModel<String, String> model, List<ReconcileProblem> errors) {
		this.model = model;
		this.errors = errors;
	}

	@Override
	public StateMachineModel<String, String> getResult() {
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
