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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.dsl.antlr.support.DefaultAntlrParseService;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.domain.CompletionItem;
import org.springframework.dsl.domain.Position;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.support.SsmlAntlrParseResultFunction;

/**
 * Tests for {@link SsmlCompletioner}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlCompletionerTests {

	@Test
	public void testStateBlockKeywords1() {
		String input = "statemachine M1 { state S1 {";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlCompletioner completioner = new SsmlCompletioner(antlrParseService, antlrParseResultFunction);
		List<CompletionItem> items = completioner.complete(document, Position.from(0, 28)).toStream().collect(Collectors.toList());
		List<String> labels = items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

		assertThat(labels, containsInAnyOrder("ENTRY", "EXIT", "INITIAL", "END", "DO"));
	}

	@Test
	public void testStateBlockKeywords2() {
		String input = "state S1 {";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlCompletioner completioner = new SsmlCompletioner(antlrParseService, antlrParseResultFunction);
		List<CompletionItem> items = completioner.complete(document, Position.from(0, 10)).toStream().collect(Collectors.toList());
		List<String> labels = items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

		assertThat(labels, containsInAnyOrder("ENTRY", "EXIT", "INITIAL", "END", "DO"));
	}

//	@Test
	public void testStateBlockKeywords3() {
		String input = "state S1{initial}state S2{}state S3{end}transition T1{source ";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlCompletioner completioner = new SsmlCompletioner(antlrParseService, antlrParseResultFunction);
		List<CompletionItem> items = completioner.complete(document, Position.from(0, 61)).toStream().collect(Collectors.toList());
		List<String> labels = items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

		assertThat(labels, containsInAnyOrder("S1", "S2", "S3"));
	}

}
