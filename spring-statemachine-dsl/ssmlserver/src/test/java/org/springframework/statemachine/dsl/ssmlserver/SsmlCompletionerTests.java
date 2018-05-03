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
package org.springframework.statemachine.dsl.ssmlserver;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.dsl.document.LanguageId;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.lsp.domain.CompletionItem;
import org.springframework.statemachine.dsl.ssml.SsmlAntlrFactory;
import org.springframework.util.StringUtils;

public class SsmlCompletionerTests {

//	@Test
	public void testStateBlockKeywords1() {
		String input = "machine M1 { state S1 {";

		TextDocument document = new TextDocument("", LanguageId.PLAINTEXT, 0, input);

		SsmlCompletioner completioner = new SsmlCompletioner(new SsmlAntlrFactory());
		List<CompletionItem> items = completioner.complete(document, null).toStream().collect(Collectors.toList());
		List<String> labels = items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

		assertThat(labels, containsInAnyOrder("entry", "exit", "initial", "end", "do", "}"));
//		assertThat(labels, containsInAnyOrder("entry", "exit", "initial", "guard", "action", "end", "do",
//				"source", "event", "}", "bean", "target"));
	}

//	@Test
	public void testStateBlockKeywords2() {
		String input = "state S1 {";

		TextDocument document = new TextDocument("", LanguageId.PLAINTEXT, 0, input);

		SsmlCompletioner completioner = new SsmlCompletioner(new SsmlAntlrFactory());
		List<CompletionItem> items = completioner.complete(document, null).toStream().collect(Collectors.toList());
		List<String> labels = items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

		assertThat(labels, containsInAnyOrder("entry", "exit", "initial", "end", "do", "}"));
//		assertThat(labels, containsInAnyOrder("entry", "exit", "initial", "guard", "action", "end", "do",
//				"source", "event", "}", "bean", "target"));
	}

//	@Test
	public void testStateBlockKeywords3() {
		String input = "state S1{initial}state S2{}state S3{end}transition T1{source";

		TextDocument document = new TextDocument("", LanguageId.PLAINTEXT, 0, input);

		SsmlCompletioner completioner = new SsmlCompletioner(new SsmlAntlrFactory());
		List<CompletionItem> items = completioner.complete(document, null).toStream().collect(Collectors.toList());
		List<String> labels = items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

		System.out.println("XXX: " + StringUtils.collectionToCommaDelimitedString(labels));

		assertThat(labels, containsInAnyOrder("S1", "S2", "S3"));
	}

}
