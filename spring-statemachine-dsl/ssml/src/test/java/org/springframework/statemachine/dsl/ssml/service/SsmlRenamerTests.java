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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.dsl.antlr.support.DefaultAntlrParseService;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.domain.Position;
import org.springframework.dsl.domain.Range;
import org.springframework.dsl.domain.TextEdit;
import org.springframework.dsl.domain.WorkspaceEdit;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.support.SsmlAntlrParseResultFunction;

/**
 * Tests for {@link SsmlRenamer}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlRenamerTests {

	@Test
	public void test1() {
		String input = "state S1 {} state S2 {} transition T1 {source S1 target S2}";

		TextDocument document = new TextDocument("", SsmlLanguage.LANGUAGE_ID, 0, input);

		DefaultAntlrParseService<StateMachineModel<String, String>> antlrParseService = new DefaultAntlrParseService<>();
		SsmlAntlrParseResultFunction antlrParseResultFunction = new SsmlAntlrParseResultFunction(SsmlLanguage.ANTRL_FACTORY);

		SsmlSymbolizer symbolizer = new SsmlSymbolizer(antlrParseService, antlrParseResultFunction);
		SsmlRenamer renamer = new SsmlRenamer(symbolizer);

		WorkspaceEdit workspaceEdit = renamer.rename(document, Position.from(0, 47), "newName").block();
		assertThat(workspaceEdit.getChanges().size(), is(1));
		assertThat(workspaceEdit.getChanges().get("").size(), is(2));
		assertThat(workspaceEdit.getChanges().get(""), containsInAnyOrder(
				TextEdit.textEdit().newText("newName").range(Range.from(0, 6, 0, 8)).build(),
				TextEdit.textEdit().newText("newName").range(Range.from(0, 46, 0, 48)).build()));
	}
}
