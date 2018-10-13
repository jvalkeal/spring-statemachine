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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.dsl.antlr.support.DefaultAntlrParseService;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.domain.DocumentSymbol;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.support.SsmlAntlrParseResultFunction;

public class SsmlSymbolizerTests {

	@Test
	public void test1() {
		String input = "";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlSymbolizer symbolizer = new SsmlSymbolizer(antlrParseService, antlrParseResultFunction);
		List<DocumentSymbol> symbols = symbolizer.symbolize(document).toStream().collect(Collectors.toList());
		List<String> labels = symbols.stream().map(symbol -> symbol.getName()).collect(Collectors.toList());

		assertThat(labels.size(), is(0));
	}

	@Test
	public void test2() {
		String input = "state S1 {}";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlSymbolizer symbolizer = new SsmlSymbolizer(antlrParseService, antlrParseResultFunction);
		List<DocumentSymbol> symbols = symbolizer.symbolize(document).toStream().collect(Collectors.toList());
		List<String> labels = symbols.stream().map(symbol -> symbol.getName()).collect(Collectors.toList());

		assertThat(labels.size(), is(1));
		assertThat(labels, containsInAnyOrder("S1"));
	}
}
