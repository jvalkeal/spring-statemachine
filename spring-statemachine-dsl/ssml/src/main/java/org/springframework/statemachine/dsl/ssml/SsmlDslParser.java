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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.dsl.DslException;
import org.springframework.statemachine.dsl.DslParser;
import org.springframework.statemachine.dsl.DslParserResult;
import org.springframework.statemachine.dsl.DslParserResultError;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ActionContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.GuardContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.MachineContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ParameterContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.StateContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.TransitionContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.FileCopyUtils;

/**
 * {@link DslParser} implementation parsing {@code SSML} content.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlDslParser implements DslParser<StateMachineModel<String, String>> {

	private StateMachineComponentResolver<String, String> resolver;

	public SsmlDslParser() {
	}

	public SsmlDslParser(StateMachineComponentResolver<String, String> resolver) {
		this.resolver = resolver;
	}

	@Override
	public DslParserResult<StateMachineModel<String, String>> parse(Resource resource) {
		String content = null;
		try {
			content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		} catch (IOException e) {
			throw new DslException(e);
		}
		CharStream antlrInputStream = CharStreams.fromString(content);
		SsmlLexer lexer = new SsmlLexer(antlrInputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);

		ArrayList<DslParserResultError> errors = new ArrayList<>();

		SsmlParser parser = new SsmlParser(tokenStream);
		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		parser.removeErrorListeners();
		parser.addErrorListener(new ErrorListener(errors));

		ParseTree tree = parser.machine();

		StateMachineVisitor stateMachineVisitor = new StateMachineVisitor(errors, resolver);

		StateMachineModel<String, String> model = stateMachineVisitor.visit(tree);

		return new SsmlDslParserResult(model, errors);
	}

	@Override
	public void setStateMachineComponentResolver(StateMachineComponentResolver<String, String> resolver) {
		this.resolver = resolver;
	}

	private static class StateMachineVisitor extends SsmlBaseVisitor<StateMachineModel<String, String>> {

		private final List<DslParserResultError> errors;
		private final StateMachineComponentResolver<String, String> resolver;

		public StateMachineVisitor(List<DslParserResultError> errors, StateMachineComponentResolver<String, String> resolver) {
			this.errors = errors;
			this.resolver = resolver;
		}

		@Override
		public StateMachineModel<String, String> visitMachine(MachineContext ctx) {
			ActionVisitor actionVisitor = new ActionVisitor(resolver);
			Map<String, Action<String, String>> actions = ctx.objectList().action().stream()
					.map(actionContext -> actionContext.accept(actionVisitor))
					.collect(Collectors.toMap(result -> result.id, result -> result.action));

			GuardVisitor guardVisitor = new GuardVisitor(resolver);
			Map<String, Guard<String, String>> guards = ctx.objectList().guard().stream()
					.map(guardContext -> guardContext.accept(guardVisitor))
					.collect(Collectors.toMap(result -> result.id, result -> result.guard));

			StateVisitor stateVisitor = new StateVisitor(actions);
			TransitionVisitor transitionVisitor = new TransitionVisitor(errors, stateVisitor, guards);


			List<StateData<String, String>> stateDatas = ctx.objectList().state().stream()
				.map(stateContext -> stateContext.accept(stateVisitor))
				.collect(Collectors.toList());

			List<TransitionData<String, String>> transitionDatas = ctx.objectList().transition().stream()
					.map(stateContext -> stateContext.accept(transitionVisitor))
					.collect(Collectors.toList());

			return new DefaultStateMachineModel<>(null, new StatesData<>(stateDatas), new TransitionsData<>(transitionDatas));
		}
	}

	private static class ActionVisitor extends SsmlBaseVisitor<ActionResult> {

		private final StateMachineComponentResolver<String, String> resolver;

		public ActionVisitor(StateMachineComponentResolver<String, String> resolver) {
			this.resolver = resolver;
		}

		@Override
		public ActionResult visitAction(ActionContext ctx) {
			String action = ctx.id().getText();

			String bean = null;
			for (ParameterContext parameterContext : ctx.parameters().parameter()) {
				if (parameterContext.type().BEAN() != null) {
					bean = parameterContext.id().getText();
				}
			}

			return new ActionResult(action, resolver.resolveAction(bean));
		}
	}

	private static class GuardVisitor extends SsmlBaseVisitor<GuardResult> {

		private final StateMachineComponentResolver<String, String> resolver;

		public GuardVisitor(StateMachineComponentResolver<String, String> resolver) {
			this.resolver = resolver;
		}

		@Override
		public GuardResult visitGuard(GuardContext ctx) {
			String guard = ctx.id().getText();

			String bean = null;
			for (ParameterContext parameterContext : ctx.parameters().parameter()) {
				if (parameterContext.type().BEAN() != null) {
					bean = parameterContext.id().getText();
				}
			}

			return new GuardResult(guard, resolver.resolveGuard(bean));
		}
	}

	private static class ActionResult {
		String id;
		Action<String, String> action;

		public ActionResult(String id, Action<String, String> action) {
			this.id = id;
			this.action = action;
		}
	}

	private static class GuardResult {
		String id;
		Guard<String, String> guard;

		public GuardResult(String id, Guard<String, String> guard) {
			this.id = id;
			this.guard = guard;
		}
	}

	private static class StateVisitor extends SsmlBaseVisitor<StateData<String, String>> {

		private final Set<String> seenStates = new HashSet<>();
		private final Map<String, Action<String, String>> actions;

		public StateVisitor(Map<String, Action<String, String>> actions) {
			this.actions = actions;
		}

		@Override
		public StateData<String, String> visitState(StateContext ctx) {
			String state = ctx.id().getText();
			seenStates.add(state);
			StateData<String, String> stateData = new StateData<>(state);
			List<Action<String, String>> exitActions = new ArrayList<>();

			for (ParameterContext parameterContext : ctx.parameters().parameter()) {
				if (parameterContext.type().INITIAL() != null) {
					stateData.setInitial(true);
				} else if (parameterContext.type().END() != null) {
					stateData.setEnd(true);
				} else if (parameterContext.type().EXIT() != null) {
					Action<String, String> action = actions.get(parameterContext.id().getText());
					if (action != null) {
						exitActions.add(action);
					}
				}
			}
			stateData.setExitActions(exitActions);

			return stateData;
		}

		public Set<String> getSeenStates() {
			return seenStates;
		}
	}

	private static class TransitionVisitor extends SsmlBaseVisitor<TransitionData<String, String>> {

		private final List<DslParserResultError> errors;
		private final StateVisitor stateVisitor;
		private final Map<String, Guard<String, String>> guards;

		public TransitionVisitor(List<DslParserResultError> errors, StateVisitor stateVisitor, Map<String, Guard<String, String>> guards) {
			this.errors = errors;
			this.stateVisitor = stateVisitor;
			this.guards = guards;
		}

		@Override
		public TransitionData<String, String> visitTransition(TransitionContext ctx) {
			String source = null;
			String target = null;
			String event = null;
			Guard<String, String> guard = null;
			for (ParameterContext parameterContext : ctx.parameters().parameter()) {
				Token idToken = parameterContext.id().ID().getSymbol();
				if (parameterContext.type().SOURCE() != null) {
					source = parameterContext.id().getText();
					if (!stateVisitor.getSeenStates().contains(source)) {
						errors.add(new SsmlTransitionSourceStateDslParserResultError(idToken));
					}
				} else if (parameterContext.type().TARGET() != null) {
					target = parameterContext.id().getText();
					if (!stateVisitor.getSeenStates().contains(target)) {
						errors.add(new SsmlTransitionTargetStateDslParserResultError(idToken));
					}
				} else if (parameterContext.type().EVENT() != null) {
					event = parameterContext.id().getText();
				} else if (parameterContext.type().GUARD() != null) {
					guard = guards.get(parameterContext.id().getText());
				}
			}
			TransitionData<String, String> transitionData = new TransitionData<String, String>(source, target, event, null, guard, TransitionKind.EXTERNAL);
			return transitionData;
		}
	}

	private static class ErrorListener extends BaseErrorListener {

		private final List<DslParserResultError> errors;

		public ErrorListener(List<DslParserResultError> errors) {
			this.errors = errors;
		}

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			errors.add(new SsmlGenericDslParserResultError(msg, line, charPositionInLine));
		}
	}
}
