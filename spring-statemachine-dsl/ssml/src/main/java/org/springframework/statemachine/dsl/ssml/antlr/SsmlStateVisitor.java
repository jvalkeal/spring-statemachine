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
import org.springframework.dsl.symboltable.DefaultSymbolTable;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.dsl.ssml.SsmlParser.IdContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.StateContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.StateParameterContext;
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
	private final DefaultSymbolTable symbolTable;

	SsmlStateVisitor(StateMachineComponentResolver<S, E> stateMachineComponentResolver,
			Map<String, Action<S, E>> actions, DefaultSymbolTable symbolTable) {
		super(stateMachineComponentResolver);
		this.actions = actions;
		this.symbolTable = symbolTable;
	}

	@Override
	public StateData<S, E> visitState(StateContext ctx) {
		ClassSymbol classSymbol = new ClassSymbol(ctx.id().getText());
		classSymbol.setSuperClass(ClassUtils.getQualifiedName(State.class));
		symbolTable.defineGlobal(classSymbol);
		IdContext id = ctx.id();

		int len = id.ID().getSymbol().getStopIndex() - id.ID().getSymbol().getStartIndex();

		classSymbol.setRange(Range.from(id.getStart().getLine() - 1, id.getStart().getCharPositionInLine(),
				id.getStop().getLine() - 1, id.getStop().getCharPositionInLine() + len));

		S state = (S) ctx.id().getText();
		seenStates.add(state);
		StateData<S, E> stateData = new StateData<>(state);
		List<Action<S, E>> exitActions = new ArrayList<>();

		for (StateParameterContext parameterContext : ctx.stateParameters().stateParameter()) {
			if (parameterContext.stateType().INITIAL() != null) {
				stateData.setInitial(true);
			} else if (parameterContext.stateType().END() != null) {
				stateData.setEnd(true);
			} else if (parameterContext.stateType().EXIT() != null) {
				Action<S, E> action = actions.get(parameterContext.id().getText());
				if (action != null) {
					exitActions.add(action);
				}
			}
		}
		stateData.setExitActions(exitActions);

		return stateData;
	}

	public Set<S> getSeenStates() {
		return seenStates;
	}
}