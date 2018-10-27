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

import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.dsl.ssml.SsmlParser.EventIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.GuardIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.SourceIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.TargetIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.TransitionContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.TransitionParameterContext;
import org.springframework.statemachine.dsl.ssml.support.SsmlTransitionSourceStateDslParserResultError;
import org.springframework.statemachine.dsl.ssml.support.SsmlTransitionTargetStateDslParserResultError;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * {@code Visitor} visiting {@link Transition} definitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class SsmlTransitionVisitor<S, E> extends AbstractSsmlBaseVisitor<S, E, TransitionData<S, E>> {

	private final List<ReconcileProblem> errors;
	private final SsmlStateVisitor<S, E> stateVisitor;
	private final Map<String, Guard<S, E>> guards;

	SsmlTransitionVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			List<ReconcileProblem> errors, SsmlStateVisitor<S, E> stateVisitor, Map<String, Guard<S, E>> guards) {
		super(stateMachineComponentResolver);
		this.errors = errors;
		this.stateVisitor = stateVisitor;
		this.guards = guards;
	}

	@Override
	public TransitionData<S, E> visitTransition(TransitionContext ctx) {
		S source = null;
		S target = null;
		E event = null;
		Guard<S, E> guard = null;
		for (TransitionParameterContext parameterContext : ctx.transitionParameters().transitionParameter()) {
			SourceIdContext sourceId = parameterContext.transitionType().sourceId();
			TargetIdContext targetId = parameterContext.transitionType().targetId();

			if (sourceId  != null) {
				source = (S) sourceId.getText();
				if (!stateVisitor.getSeenStates().contains(source)) {
					errors.add(new SsmlTransitionSourceStateDslParserResultError(sourceId.ID().getSymbol()));
				}
			}
			if (targetId  != null) {
				target = (S) targetId.getText();
				if (!stateVisitor.getSeenStates().contains(target)) {
					errors.add(new SsmlTransitionTargetStateDslParserResultError(targetId.ID().getSymbol()));
				}
			}
			if (parameterContext.transitionType().EVENT() != null) {
				EventIdContext eventId = parameterContext.transitionType().eventId();
				if (eventId != null) {
					event = (E) eventId.getText();
				}
			}
			if (parameterContext.transitionType().GUARD() != null) {
				GuardIdContext guardId = parameterContext.transitionType().guardId();
				if (guardId != null) {
					guard = guards.get(guardId.getText());
				}

			}
		}
		TransitionData<S, E> transitionData = new TransitionData<>(source, target, event, null, guard, TransitionKind.EXTERNAL);
		return transitionData;
	}
}