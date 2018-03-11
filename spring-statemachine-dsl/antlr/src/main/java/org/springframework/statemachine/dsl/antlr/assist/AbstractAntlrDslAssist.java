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
package org.springframework.statemachine.dsl.antlr.assist;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.springframework.statemachine.dsl.DslAssist;
import org.springframework.statemachine.dsl.DslException;
import org.springframework.statemachine.dsl.antlr.AntlrFactory;
import org.springframework.statemachine.dsl.assist.AbstractDslAssist;
import org.springframework.util.Assert;

/**
 * Base abstract implementation of a {@link DslAssist} providing common shared
 * features using {@code ANTLR}.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class AbstractAntlrDslAssist extends AbstractDslAssist {

	private final AntlrFactory antlrFactory;

    /**
     * Instantiate a new Abstract Antlr Dsl Assist.
     *
     * @param antlrFactory the Antlr Factory
     */
    public AbstractAntlrDslAssist(AntlrFactory antlrFactory) {
		super();
		Assert.notNull(antlrFactory, "antlrFactory must be set");
		this.antlrFactory = antlrFactory;
	}

    protected AntlrFactory getAntlrFactory() {
		return antlrFactory;
	}

    protected Lexer createLexer(String content) {
        return this.antlrFactory.createLexer(stringToCharStream(content));
    }

    protected Parser createParser() {
    	return this.antlrFactory.createParser(null);
    }

	protected static List<? extends Token> filterTokensByChannel(List<? extends Token> tokens, int channel) {
        return tokens.stream().filter(t -> t.getChannel() == channel).collect(Collectors.toList());
    }

	private static CharStream stringToCharStream(String content) {
        try {
            return CharStreams.fromReader(new StringReader(content));
        } catch (IOException e) {
            throw new DslException( e);
        }
    }
}
