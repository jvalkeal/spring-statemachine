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

import java.util.Collection;

import org.springframework.dsl.antlr.AbstractAntlrCompletioner;
import org.springframework.dsl.antlr.AntlrFactory;
import org.springframework.dsl.lsp.domain.CompletionItem;
import org.springframework.dsl.lsp.service.Completioner;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link Completioner} implementation for a {@code SSML} language.
 *
 * @author Janne Valkealahti
 * @see EnableSsmlLanguage
 *
 */
public class SsmlCompletioner extends AbstractAntlrCompletioner<SsmlLexer, SsmlParser> {

	public SsmlCompletioner(AntlrFactory<SsmlLexer, SsmlParser> antlrFactory) {
		super(antlrFactory);
	}

	@Override
	protected Flux<CompletionItem> completeInternal(String content) {
		return Flux.fromIterable(assistCompletions(content))
			.flatMap(c -> {
				CompletionItem item = new CompletionItem();
				item.setLabel(c);
				return Mono.just(item);
			});
	}
}
