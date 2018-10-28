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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dsl.domain.Range;
import org.springframework.dsl.symboltable.ClassSymbol;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ActionIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ExitIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.IdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ParentIdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.StateContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.StateParameterContext;
import org.springframework.statemachine.dsl.ssml.support.SsmlSymbolTable;
import org.springframework.statemachine.state.State;
import org.springframework.util.ClassUtils;

/**
 * {@code Visitor} visiting {@link State} definitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class SsmlStateVisitor<S, E> extends AbstractSsmlBaseVisitor<S, E, StateData<S, E>> {

	private final Set<S> seenStates = new HashSet<>();
	private final Map<String, Action<S, E>> actions;

	SsmlStateVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			Map<String, Action<S, E>> actions, SsmlSymbolTable symbolTable) {
		super(stateMachineComponentResolver, symbolTable);
		this.actions = actions;
	}

	@Override
	public StateData<S, E> visitState(StateContext ctx) {
		IdContext id = ctx.id();
		if (id != null) {
			ClassSymbol classSymbol = new ClassSymbol(id.getText());
			classSymbol.setSuperClass(ClassUtils.getQualifiedName(State.class));
			getSymbolTable().defineGlobal(classSymbol);
			int len = id.ID().getSymbol().getStopIndex() - id.ID().getSymbol().getStartIndex();
			classSymbol.setRange(Range.from(id.getStart().getLine() - 1, id.getStart().getCharPositionInLine(),
					id.getStop().getLine() - 1, id.getStop().getCharPositionInLine() + len));
		}


		S state = getStateMapperFunction().apply(ctx.id().getText());
		seenStates.add(state);
		StateData<S, E> stateData = new StateData<>(state);
		List<Action<S, E>> exitActions = new ArrayList<>();
		Action<S, E> initialAction = null;

		for (StateParameterContext parameterContext : ctx.stateParameters().stateParameter()) {

			ParentIdContext parentId = parameterContext.stateType().parentId();

			if (parameterContext.stateType().INITIAL() != null) {
				stateData.setInitial(true);
				ActionIdContext actionId = parameterContext.stateType().actionId();
				if (actionId != null) {
					initialAction = actions.get(actionId.getText());
					if (initialAction != null) {
						stateData.setInitialAction(initialAction);
					}
				}
			} else if (parameterContext.stateType().END() != null) {
				stateData.setEnd(true);
			} else if (parameterContext.stateType().EXIT() != null) {
				ExitIdContext exitId = parameterContext.stateType().exitId();
				if (exitId != null) {
					Action<S, E> action = actions.get(exitId.getText());
					if (action != null) {
						exitActions.add(action);
					}
				}
			}

			if (parentId != null) {
				S parent = getStateMapperFunction().apply(parentId.getText());
				stateData.setParent(parent);
			}

		}
		stateData.setExitActions(exitActions);
		return stateData;
	}

	public Set<S> getSeenStates() {
		return seenStates;
	}
}