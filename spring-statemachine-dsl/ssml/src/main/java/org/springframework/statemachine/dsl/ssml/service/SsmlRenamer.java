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

import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.domain.DocumentSymbol;
import org.springframework.dsl.domain.Position;
import org.springframework.dsl.domain.TextEdit;
import org.springframework.dsl.domain.WorkspaceEdit;
import org.springframework.dsl.service.AbstractDslService;
import org.springframework.dsl.service.Renamer;
import org.springframework.dsl.service.symbol.Symbolizer;
import org.springframework.dsl.support.DslUtils;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link Renamer} for a {@code ssml} language.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlRenamer extends AbstractDslService implements Renamer {

	private final Symbolizer symbolizer;
	private final static Function<DocumentSymbol, Publisher<DocumentSymbol>> expander =
			ds -> (ds.getChildren() == null || ds.getChildren().isEmpty())
				? Flux.empty()
				: Flux.fromIterable(ds.getChildren());

	/**
	 * Instantiates a new ssml renamer.
	 *
	 * @param symbolizer the symbolizer
	 */
	public SsmlRenamer(Symbolizer symbolizer) {
		super(SsmlLanguage.LANGUAGE_ID);
		Assert.notNull(symbolizer, "symbolizer must be set");
		this.symbolizer = symbolizer;
	}

	@Override
	public Mono<WorkspaceEdit> rename(Document document, Position position, String newName) {
		Flux<DocumentSymbol> symbols = symbolizer.symbolize(document)
				.expandDeep(expander);
		Mono<DocumentSymbol> symbol = symbols
				.filter(s -> DslUtils.isPositionInRange(position, s.getRange()))
				.next();
		return symbols
				.filterWhen(s -> symbol.map(sym -> ObjectUtils.nullSafeEquals(s.getName(), sym.getName())))
				.map(s -> TextEdit.textEdit()
					.newText(newName)
					.range(s.getRange())
					.build())
				.collectList()
				.map(list -> {
					return WorkspaceEdit.workspaceEdit()
						.changes(document.uri(), list)
						.build();
				});
	}
}
