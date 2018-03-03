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
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.dsl.DslException;
import org.springframework.statemachine.dsl.DslParser;
import org.springframework.statemachine.dsl.DslParserResult;
import org.springframework.statemachine.dsl.DslParserResultError;
import org.springframework.statemachine.dsl.ssml.SsmlParser.MachineContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.ParameterContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.StateContext;
import org.springframework.statemachine.dsl.ssml.SsmlParser.TransitionContext;
import org.springframework.util.FileCopyUtils;

/**
 * {@link DslParser} implementation parsing {@code SSML} content.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlDslParser implements DslParser<StateMachineModel<String, String>> {

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

		StateMachineVisitor stateMachineVisitor = new StateMachineVisitor(errors);

		StateMachineModel<String, String> model = stateMachineVisitor.visit(tree);

		return new SsmlDslParserResult(model, errors);
	}

	private static class StateMachineVisitor extends SsmlBaseVisitor<StateMachineModel<String, String>> {

		private final List<DslParserResultError> errors;

		public StateMachineVisitor(List<DslParserResultError> errors) {
			this.errors = errors;
		}

		@Override
		public StateMachineModel<String, String> visitMachine(MachineContext ctx) {
			StateVisitor stateVisitor = new StateVisitor();
			TransitionVisitor transitionVisitor = new TransitionVisitor(errors, stateVisitor);

			List<StateData<String, String>> stateDatas = ctx.objectList().state().stream()
				.map(stateContext -> stateContext.accept(stateVisitor))
				.collect(Collectors.toList());

			List<TransitionData<String, String>> transitionDatas = ctx.objectList().transition().stream()
					.map(stateContext -> stateContext.accept(transitionVisitor))
					.collect(Collectors.toList());

			return new DefaultStateMachineModel<>(null, new StatesData<>(stateDatas), new TransitionsData<>(transitionDatas));
		}
	}

	private static class StateVisitor extends SsmlBaseVisitor<StateData<String, String>> {

		private final Set<String> seenStates = new HashSet<>();

		@Override
		public StateData<String, String> visitState(StateContext ctx) {
			String state = ctx.id().getText();
			seenStates.add(state);
			StateData<String, String> stateData = new StateData<>(state);

			for (ParameterContext parameterContext : ctx.parameters().parameter()) {
				if (parameterContext.type().INITIAL() != null) {
					stateData.setInitial(true);
				} else if (parameterContext.type().END() != null) {
					stateData.setEnd(true);
				}
			}

			return stateData;
		}

		public Set<String> getSeenStates() {
			return seenStates;
		}
	}

	private static class TransitionVisitor extends SsmlBaseVisitor<TransitionData<String, String>> {

		private final List<DslParserResultError> errors;
		private final StateVisitor stateVisitor;

		public TransitionVisitor(List<DslParserResultError> errors, StateVisitor stateVisitor) {
			this.errors = errors;
			this.stateVisitor = stateVisitor;
		}

		@Override
		public TransitionData<String, String> visitTransition(TransitionContext ctx) {
			String source = null;
			String target = null;
			String event = null;
			for (ParameterContext parameterContext : ctx.parameters().parameter()) {
				Token idToken = parameterContext.id().ID().getSymbol();
				if (parameterContext.type().SOURCE() != null) {
					source = parameterContext.id().getText();
				} else if (parameterContext.type().TARGET() != null) {

					target = parameterContext.id().getText();
					if (!stateVisitor.getSeenStates().contains(target)) {
						errors.add(new SsmlTransitionTargetStateDslParserResultError(idToken));
					}
				} else if (parameterContext.type().EVENT() != null) {
					event = parameterContext.id().getText();
				}
			}
			TransitionData<String, String> transitionData = new TransitionData<String, String>(source, target, event);
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
