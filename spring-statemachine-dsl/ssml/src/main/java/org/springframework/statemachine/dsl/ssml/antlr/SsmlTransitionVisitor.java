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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ActionIdContext;
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

	private static final Log log = LogFactory.getLog(SsmlTransitionVisitor.class);
	private final List<ReconcileProblem> errors;
	private final SsmlStateVisitor<S, E> stateVisitor;
	private final Map<String, Guard<S, E>> guards;
	private final Map<String, Action<S, E>> actions;

	SsmlTransitionVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			List<ReconcileProblem> errors, SsmlStateVisitor<S, E> stateVisitor, Map<String, Guard<S, E>> guards,
			Map<String, Action<S, E>> actions) {
		super(stateMachineComponentResolver);
		this.errors = errors;
		this.stateVisitor = stateVisitor;
		this.guards = guards;
		this.actions = actions;
	}

	@Override
	public TransitionData<S, E> visitTransition(TransitionContext ctx) {
		S source = null;
		S target = null;
		E event = null;
		Guard<S, E> resolvedGuard = null;
		List<Action<S, E>> resolvedActions = new ArrayList<>();
		TransitionKind kind = TransitionKind.EXTERNAL;
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
					resolvedGuard = guards.get(guardId.getText());
					log.debug("visitTransition guard=" + resolvedGuard);
				}
			}

			if (parameterContext.transitionType().ACTION() != null) {
				ActionIdContext actionId = parameterContext.transitionType().actionId();
				if (actionId != null) {
					Action<S, E> action = actions.get(actionId.getText());
					if (action != null) {
						resolvedActions.add(action);
						log.debug("visitTransition action=" + action);
					}
				}
			}

			if (parameterContext.transitionType().EXTERNAL() != null) {
				kind = TransitionKind.EXTERNAL;
			} else if (parameterContext.transitionType().INTERNAL() != null) {
				kind = TransitionKind.INTERNAL;
			} else if (parameterContext.transitionType().LOCAL() != null) {
				kind = TransitionKind.LOCAL;
			}
		}
		log.debug("visitTransition kind=" + kind);
		return new TransitionData<>(source, target, event, resolvedActions, resolvedGuard, kind);
	}
}