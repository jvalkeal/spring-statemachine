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

import java.util.Arrays;
import java.util.function.Function;

import org.springframework.dsl.antlr.AntlrParseResult;
import org.springframework.dsl.antlr.AntlrParseService;
import org.springframework.dsl.antlr.support.AbstractAntlrCompletioner;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.service.Completioner;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.EnableSsmlLanguage;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;

import reactor.core.publisher.Mono;

/**
 * A {@link Completioner} implementation for a {@code SSML} language.
 *
 * @author Janne Valkealahti
 * @see EnableSsmlLanguage
 *
 */
public class SsmlCompletioner extends AbstractAntlrCompletioner<StateMachineModel<String, String>> {

	public SsmlCompletioner(AntlrParseService<StateMachineModel<String, String>> antlrParseService,
			Function<Document, Mono<? extends AntlrParseResult<StateMachineModel<String, String>>>> antlrParseResultSupplier) {
		super(Arrays.asList(SsmlLanguage.LANGUAGE_ID), antlrParseService, antlrParseResultSupplier);
	}

//	public SsmlCompletioner(AntlrFactory<SsmlLexer, SsmlParser> antlrFactory) {
//		super(antlrFactory);
//	}
//
//	@Override
//	protected SsmlParser getParser(String input) {
//		SsmlLexer lexer = getAntlrFactory().createLexer(CharStreams.fromString(input));
//		SsmlParser parser = getAntlrFactory().createParser(new CommonTokenStream(lexer));
//		parser.definitions();
//		return parser;
//	}
//
//	@Override
//	protected Flux<CompletionItem> completeInternal(String content) {
//		return Flux.fromIterable(assistCompletions(content))
//			.flatMap(c -> {
//				CompletionItem item = new CompletionItem();
//				item.setLabel(c);
//				return Mono.just(item);
//			});
//	}
}
