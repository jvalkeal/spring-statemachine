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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.core.io.Resource;
import org.springframework.dsl.DslException;
import org.springframework.dsl.DslParserResult;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.StateMachineDslParser;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlErrorListener;
import org.springframework.statemachine.dsl.ssml.antlr.SsmlStateMachineVisitor;
import org.springframework.util.FileCopyUtils;

/**
 * {@code DslParser} implementation parsing {@code SSML} content.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlDslParser implements StateMachineDslParser<String, String, StateMachineModel<String, String>> {

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

		ArrayList<ReconcileProblem> errors = new ArrayList<>();

		SsmlParser parser = new SsmlParser(tokenStream);
		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		parser.removeErrorListeners();
		parser.addErrorListener(new SsmlErrorListener(errors));

		ParseTree tree = parser.definitions();

		SsmlStateMachineVisitor<String, String> stateMachineVisitor = new SsmlStateMachineVisitor<>(errors, resolver);

		StateMachineModel<String, String> model = stateMachineVisitor.visit(tree);

		return new SsmlDslParserResult(model, errors);
	}

	@Override
	public void setStateMachineComponentResolver(StateMachineComponentResolver<String, String> resolver) {
		this.resolver = resolver;
	}
}
