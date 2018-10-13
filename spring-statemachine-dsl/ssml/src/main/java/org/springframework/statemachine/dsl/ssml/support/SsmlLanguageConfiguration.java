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
package org.springframework.statemachine.dsl.ssml.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dsl.antlr.AntlrFactory;
import org.springframework.dsl.antlr.AntlrParseService;
import org.springframework.dsl.antlr.support.DefaultAntlrParseService;
import org.springframework.dsl.service.Completioner;
import org.springframework.dsl.service.Hoverer;
import org.springframework.dsl.service.reconcile.Linter;
import org.springframework.dsl.service.symbol.Symbolizer;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.ssml.EnableSsmlLanguage;
import org.springframework.statemachine.dsl.ssml.SsmlLanguage;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.statemachine.dsl.ssml.service.SsmlCompletioner;
import org.springframework.statemachine.dsl.ssml.service.SsmlHoverer;
import org.springframework.statemachine.dsl.ssml.service.SsmlLinter;
import org.springframework.statemachine.dsl.ssml.service.SsmlSymbolizer;

/**
 * Configuration for a {@code simple} sample language supporting
 * {@link Hoverer}, {@link Completioner}, {@link Symbolizer} and {@link Linter}.
 *
 * @author Janne Valkealahti
 * @see EnableSsmlLanguage
 *
 */
@Configuration
public class SsmlLanguageConfiguration {

	@Bean
	public Hoverer ssmlHoverer() {
		return new SsmlHoverer(ssmlAntlrParseService(), ssmlAntlrParseResultFunction());
	}

	@Bean
	public Completioner ssmlCompletioner() {
		return new SsmlCompletioner(ssmlAntlrParseService(), ssmlAntlrParseResultFunction());
	}

	@Bean
	public Linter ssmlLinter() {
		return new SsmlLinter(ssmlAntlrParseService(), ssmlAntlrParseResultFunction());
	}

	@Bean
	public Symbolizer ssmlSymbolizer() {
		return new SsmlSymbolizer(ssmlAntlrParseService(), ssmlAntlrParseResultFunction());
	}

	@Bean
	public AntlrParseService<StateMachineModel<String, String>> ssmlAntlrParseService() {
		return new DefaultAntlrParseService<>();
	}

	@Bean
	public SsmlAntlrParseResultFunction ssmlAntlrParseResultFunction() {
		return new SsmlAntlrParseResultFunction(ssmlAntlrFactory());
	}

	@Bean
	public AntlrFactory<SsmlLexer, SsmlParser> ssmlAntlrFactory() {
		return SsmlLanguage.ANTRL_FACTORY;
	}
}
