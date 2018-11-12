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

import org.springframework.dsl.antlr.AntlrParseResult;
import org.springframework.dsl.antlr.AntlrParseService;
import org.springframework.dsl.antlr.support.AbstractAntlrHoverer;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.service.Hoverer;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.EnableSsmlLanguage;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;

import reactor.core.publisher.Mono;

/**
 * A {@link Hoverer} implementation for a {@code simple} sample language.
 *
 * @author Janne Valkealahti
 * @see EnableSsmlLanguage
 *
 */
public class SsmlHoverer extends AbstractAntlrHoverer<StateMachineModel<String, String>> implements Hoverer {

	/**
	 * Instantiates a new ssml hoverer.
	 *
	 * @param antlrParseService the antlr parse service
	 * @param antlrParseResultFunction the antlr parse result function
	 */
	public SsmlHoverer(AntlrParseService<StateMachineModel<String, String>> antlrParseService,
			Function<Document, Mono<? extends AntlrParseResult<StateMachineModel<String, String>>>> antlrParseResultFunction) {
		super(SsmlLanguage.LANGUAGE_ID, antlrParseService, antlrParseResultFunction);
	}
}
