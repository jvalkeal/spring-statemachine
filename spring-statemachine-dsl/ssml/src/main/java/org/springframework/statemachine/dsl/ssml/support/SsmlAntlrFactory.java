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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.springframework.dsl.antlr.AntlrFactory;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;

/**
 * {@code SSML} related implementation of a {@link AntlrFactory}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlAntlrFactory implements AntlrFactory<SsmlLexer, SsmlParser> {

	@Override
	public SsmlLexer createLexer(CharStream input) {
		return new SsmlLexer(input);
	}

	@Override
	public SsmlParser createParser(TokenStream tokenStream) {
		return new SsmlParser(tokenStream);
	}
}
