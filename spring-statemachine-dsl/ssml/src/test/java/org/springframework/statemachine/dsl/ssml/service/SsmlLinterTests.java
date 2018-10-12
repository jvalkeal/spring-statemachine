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
package org.springframework.statemachine.dsl.ssml.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.dsl.antlr.support.DefaultAntlrParseService;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.service.reconcile.ReconcileProblem;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.support.SsmlAntlrParseResultFunction;

import reactor.core.publisher.Flux;

/**
 * Tests for {@link SsmlLinter}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlLinterTests {

	@Test
	public void testEmpty() {
		String input = "";
		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlLinter ssmlLinter = new SsmlLinter(antlrParseService, antlrParseResultFunction);
		Flux<ReconcileProblem> lints = ssmlLinter.lint(document);
		List<ReconcileProblem> problems = lints.toStream().collect(Collectors.toList());
		assertThat(problems, notNullValue());
		assertThat(problems.size(), is(0));
	}

}
