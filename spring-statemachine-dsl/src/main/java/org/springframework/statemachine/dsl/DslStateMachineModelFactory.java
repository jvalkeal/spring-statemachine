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
package org.springframework.statemachine.dsl;

import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.util.Assert;

/**
 * A {@link StateMachineModelFactory} which uses expected contract with
 * {@link DslParser} to build {@link StateMachineModel}.
 *
 * @author Janne Valkealahti
 *
 */
public class DslStateMachineModelFactory implements StateMachineModelFactory<String, String> {

	private final Resource resource;
	private final DslParser<StateMachineModel<String, String>> dslParser;

	/**
	 * Instantiate a dsl state machine factory.
	 *
	 * @param resource the resource to parse
	 * @param dslParser the dsl parser to use
	 */
	public DslStateMachineModelFactory(Resource resource, DslParser<StateMachineModel<String, String>> dslParser) {
		Assert.notNull(resource, "Resource must be set");
		Assert.notNull(dslParser, "DslParser must be set");
		this.resource = resource;
		this.dslParser = dslParser;
	}

	@Override
	public StateMachineModel<String, String> build() {
		return dslParser.parse(resource).getModel();
	}

	@Override
	public StateMachineModel<String, String> build(String machineId) {
		return build();
	}
}
