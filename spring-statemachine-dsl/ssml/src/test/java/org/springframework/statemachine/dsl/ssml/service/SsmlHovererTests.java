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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.dsl.antlr.support.DefaultAntlrParseService;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.domain.Hover;
import org.springframework.dsl.domain.Position;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.support.SsmlAntlrParseResultFunction;

/**
 * Tests for {@link SsmlHoverer}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlHovererTests {

//	@Test
	public void test1() {
		String input = "state S1 {}";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlHoverer hoverer = new SsmlHoverer(antlrParseService, antlrParseResultFunction);
		Hover hover = hoverer.hover(document, Position.from(0, 7)).block();
		assertThat(hover, notNullValue());
		assertThat(hover.getContents().getValue(), containsString("XXX"));
	}

}
