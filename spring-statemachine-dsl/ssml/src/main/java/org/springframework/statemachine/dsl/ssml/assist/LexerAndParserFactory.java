package org.springframework.statemachine.dsl.ssml.assist;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

public interface LexerAndParserFactory {

    Lexer createLexer(CharStream input);

    Parser createParser(TokenStream tokenStream);
}
