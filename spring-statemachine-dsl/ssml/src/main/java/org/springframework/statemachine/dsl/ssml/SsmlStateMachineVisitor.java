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
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.dsl.DslParserResultError;
import org.springframework.statemachine.dsl.ssml.SsmlParser.MachineContext;
import org.springframework.statemachine.guard.Guard;

/**
 * {@code Visitor} visiting top level state machine definition.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class SsmlStateMachineVisitor<S, E> extends SsmlBaseVisitor<StateMachineModel<S, E>> {

	private final List<DslParserResultError> errors;
	private final StateMachineComponentResolver<S, E> resolver;

	SsmlStateMachineVisitor(List<DslParserResultError> errors, StateMachineComponentResolver<S, E> resolver) {
		this.errors = errors;
		this.resolver = resolver;
	}

	@Override
	public StateMachineModel<S, E> visitMachine(MachineContext ctx) {
		SsmlActionVisitor<S, E> actionVisitor = new SsmlActionVisitor<>(resolver);
		Map<String, Action<S, E>> actions = ctx.objectList().action().stream()
				.map(actionContext -> actionContext.accept(actionVisitor))
				.collect(Collectors.toMap(result -> result.id, result -> result.action));

		SsmlGuardVisitor<S, E> guardVisitor = new SsmlGuardVisitor<>(resolver);
		Map<String, Guard<S, E>> guards = ctx.objectList().guard().stream()
				.map(guardContext -> guardContext.accept(guardVisitor))
				.collect(Collectors.toMap(result -> result.id, result -> result.guard));

		SsmlStateVisitor<S, E> stateVisitor = new SsmlStateVisitor<>(resolver, actions);
		SsmlTransitionVisitor<S, E> transitionVisitor = new SsmlTransitionVisitor<>(resolver, errors, stateVisitor, guards);


		List<StateData<S, E>> stateDatas = ctx.objectList().state().stream()
			.map(stateContext -> stateContext.accept(stateVisitor))
			.collect(Collectors.toList());

		List<TransitionData<S, E>> transitionDatas = ctx.objectList().transition().stream()
				.map(stateContext -> stateContext.accept(transitionVisitor))
				.collect(Collectors.toList());

		return new DefaultStateMachineModel<S, E>(null, new StatesData<>(stateDatas), new TransitionsData<>(transitionDatas));
	}
}