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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.AtomTransition;
import org.antlr.v4.runtime.atn.SetTransition;
import org.antlr.v4.runtime.atn.Transition;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.dsl.DslException;
import org.springframework.statemachine.dsl.antlr.AntlrFactory;
import org.springframework.util.StringUtils;

public class AntlrDslAssist {

	private static final Log log = LogFactory.getLog(OldAntlrDslAssist.class);
	private final AntlrFactory antlrFactory;

//    private final String input;
    private final Set<String> collectedSuggestions = new HashSet<>();
    private List<? extends Token> inputTokens;
//    private String untokenizedText = "";
    private ATN parserAtn;
    private String[] parserRuleNames;
    private String indent = "";
    private CasePreference casePreference = CasePreference.BOTH;

    public AntlrDslAssist(AntlrFactory antlrFactory/*, String input*/) {
    	this.antlrFactory = antlrFactory;
//    	this.input = input;
    }

    public void setCasePreference(CasePreference casePreference) {
        this.casePreference = casePreference;
    }

//    @Override
	public Collection<String> assistCompletions(String content) {
		AssistHolder assistHolder = new AssistHolder(content);
        tokenizeInput(assistHolder);
        storeParserAtnAndRuleNames();
        runParserAtnAndCollectSuggestions(assistHolder);
        return collectedSuggestions;
    }

    private void tokenizeInput(AssistHolder assistHolder) {
        Lexer lexer = createLexerWithUntokenizedTextDetection(assistHolder); // side effect: also fills this.untokenizedText
        List<? extends Token> allTokens = lexer.getAllTokens();
        this.inputTokens = filterOutNonDefaultChannels(allTokens);

//        if (log.isDebugEnabled()) {
//            log.debug("TOKENS FOUND IN FIRST PASS:");
//            for (Token token : inputTokens) {
//                log.debug(token.toString());
//            }
//        }
    }


    private void storeParserAtnAndRuleNames() {
        Parser parserForAtnOnly = antlrFactory.createParser(null);
//        logger.debug("Parser rule names: " + StringUtils.join(parserForAtnOnly.getRuleNames(), ", "));
//        log.debug("Parser rule names: " + StringUtils.arrayToCommaDelimitedString(parserForAtnOnly.getRuleNames()));
        parserAtn = parserForAtnOnly.getATN();
        parserRuleNames = parserForAtnOnly.getRuleNames();
    }

    private void runParserAtnAndCollectSuggestions(AssistHolder assistHolder) {
        ATNState initialState = parserAtn.states.get(0);
        log.debug("Parser initial state: " + initialState);
        parseAndCollectTokenSuggestions(assistHolder, initialState, 0);
    }

    /**
     * Recursive through the parser ATN to process all tokens. When successful (out of tokens) - collect completion
     * suggestions.
     */
    private void parseAndCollectTokenSuggestions(AssistHolder assistHolder, ATNState parserState, int tokenListIndex) {
        indent = indent + "  ";
        try {
            log.debug(indent + "State: " + toString(parserState) );
            log.debug(indent + "State available transitions: " + transitionsStr(assistHolder, parserState));

            if (!haveMoreTokens(tokenListIndex)) { // stop condition for recursion
                suggestNextTokensForParserState(assistHolder, parserState);
                return;
            }
            for (Transition trans : parserState.getTransitions()) {
                if (trans.isEpsilon()) {
                    handleEpsilonTransition(assistHolder, trans, tokenListIndex);
                } else if (trans instanceof AtomTransition) {
                    handleAtomicTransition(assistHolder, (AtomTransition) trans, tokenListIndex);
                } else {
                    handleSetTransition(assistHolder, (SetTransition)trans, tokenListIndex);
                }
            }
        } finally {
            indent = indent.substring(2);
        }
    }

    private boolean haveMoreTokens(int tokenListIndex) {
        return tokenListIndex < inputTokens.size();
    }

    private void handleEpsilonTransition(AssistHolder assistHolder, Transition trans, int tokenListIndex) {
        // Epsilon transitions don't consume a token, so don't move the index
        parseAndCollectTokenSuggestions(assistHolder, trans.target, tokenListIndex);
    }

    private void handleAtomicTransition(AssistHolder assistHolder, AtomTransition trans, int tokenListIndex) {
        Token nextToken = inputTokens.get(tokenListIndex);
        int nextTokenType = inputTokens.get(tokenListIndex).getType();
        boolean nextTokenMatchesTransition = (trans.label == nextTokenType);
        if (nextTokenMatchesTransition) {
            log.debug(indent + "Token " + nextToken + " following transition: " + toString(assistHolder, trans));
            parseAndCollectTokenSuggestions(assistHolder, trans.target, tokenListIndex + 1);
        } else {
            log.debug(indent + "Token " + nextToken + " NOT following transition: " + toString(assistHolder, trans));
        }
    }

    private void handleSetTransition(AssistHolder assistHolder, SetTransition trans, int tokenListIndex) {
        Token nextToken = inputTokens.get(tokenListIndex);
        int nextTokenType = nextToken.getType();
        for (int transitionTokenType : trans.label().toList()) {
            boolean nextTokenMatchesTransition = (transitionTokenType == nextTokenType);
            if (nextTokenMatchesTransition) {
                log.debug(indent + "Token " + nextToken + " following transition: " + toString(assistHolder, trans) + " to " + transitionTokenType);
                parseAndCollectTokenSuggestions(assistHolder, trans.target, tokenListIndex + 1);
            } else {
                log.debug(indent + "Token " + nextToken + " NOT following transition: " + toString(assistHolder, trans) + " to " + transitionTokenType);
            }
        }
    }

    private void suggestNextTokensForParserState(AssistHolder assistHolder, ATNState parserState) {
        Set<Integer> transitionLabels = new HashSet<>();
        fillParserTransitionLabels(parserState, transitionLabels, new HashSet<>());
        AntlrDslTokenAssist tokenSuggester = new AntlrDslTokenAssist(createLexer(assistHolder.content), this.casePreference);
        String untokenizedText = assistHolder.errorListener.errorPosition != null ? assistHolder.content.substring(assistHolder.errorListener.errorPosition) : "";
//        String untokenizedText = assistHolder.content.substring(assistHolder.errorListener.errorPosition);
        Collection<String> suggestions = tokenSuggester.suggest(transitionLabels, untokenizedText);
        parseSuggestionsAndAddValidOnes(assistHolder, parserState, suggestions);
        log.debug(indent + "WILL SUGGEST TOKENS FOR STATE: " + parserState);
    }

    private void fillParserTransitionLabels(ATNState parserState, Collection<Integer> result, Set<TransitionHolder> visitedTransitions) {
        for (Transition trans : parserState.getTransitions()) {
            TransitionHolder transWrapper = new TransitionHolder(parserState, trans);
            if (visitedTransitions.contains(transWrapper)) {
                log.debug(indent + "Not following visited " + transWrapper);
                continue;
            }
            if (trans.isEpsilon()) {
                try {
                    visitedTransitions.add(transWrapper);
                    fillParserTransitionLabels(trans.target, result, visitedTransitions);
                } finally {
                    visitedTransitions.remove(transWrapper);
                }
            } else if (trans instanceof AtomTransition) {
                int label = ((AtomTransition) trans).label;
                if (label >= 1) { // EOF would be -1
                    result.add(label);
                }
            } else if (trans instanceof SetTransition) {
                for (Interval interval : ((SetTransition) trans).label().getIntervals()) {
                    for (int i = interval.a; i <= interval.b; ++i) {
                        result.add(i);
                    }
                }
            }
        }
    }

    private void parseSuggestionsAndAddValidOnes(AssistHolder assistHolder, ATNState parserState, Collection<String> suggestions) {
        for (String suggestion : suggestions) {
            log.debug("CHECKING suggestion: " + suggestion);
            Token addedToken = getAddedToken(assistHolder, suggestion);
            if (isParseableWithAddedToken(assistHolder, parserState, addedToken, new HashSet<TransitionHolder>())) {
                collectedSuggestions.add(suggestion);
            } else {
                log.debug("DROPPING non-parseable suggestion: " + suggestion);
            }
        }
    }

    private Token getAddedToken(AssistHolder assistHolder, String suggestedCompletion) {
//        String completedText = this.input + suggestedCompletion;
        String completedText = assistHolder.content + suggestedCompletion;
        Lexer completedTextLexer = this.createLexer(completedText);
        completedTextLexer.removeErrorListeners();
        List<? extends Token> completedTextTokens = filterOutNonDefaultChannels(completedTextLexer.getAllTokens());
        if (completedTextTokens.size() <= inputTokens.size()) {
            return null; // Completion didn't yield whole token, could be just a token fragment
        }
        log.debug("TOKENS IN COMPLETED TEXT: " + completedTextTokens);
        Token newToken = completedTextTokens.get(completedTextTokens.size() - 1);
        return newToken;
    }

    private boolean isParseableWithAddedToken(AssistHolder assistHolder, ATNState parserState, Token newToken, Set<TransitionHolder> visitedTransitions) {
        if (newToken == null) {
            return false;
        }
        for (Transition parserTransition : parserState.getTransitions()) {
            if (parserTransition.isEpsilon()) { // Recurse through any epsilon transitionsStr
                TransitionHolder transWrapper = new TransitionHolder(parserState, parserTransition);
                if (visitedTransitions.contains(transWrapper)) {
                    continue;
                }
                visitedTransitions.add(transWrapper);
                try {
                    if (isParseableWithAddedToken(assistHolder, parserTransition.target, newToken, visitedTransitions)) {
                        return true;
                    }
                } finally {
                    visitedTransitions.remove(transWrapper);
                }
            } else if (parserTransition instanceof AtomTransition) {
                AtomTransition parserAtomTransition = (AtomTransition) parserTransition;
                int transitionTokenType = parserAtomTransition.label;
                if (transitionTokenType == newToken.getType()) {
                    return true;
                }
            } else if (parserTransition instanceof SetTransition) {
                SetTransition parserSetTransition = (SetTransition) parserTransition;
                for (int transitionTokenType : parserSetTransition.label().toList()) {
                    if (transitionTokenType == newToken.getType()) {
                        return true;
                    }
                }
            } else {
                throw new IllegalStateException("Unexpected: " + toString(assistHolder, parserTransition));
            }
        }
        return false;
    }

    private String toString(ATNState parserState) {
        String ruleName = this.parserRuleNames[parserState.ruleIndex];
        return ruleName + " " + parserState.getClass().getSimpleName() + " " + parserState;
    }

    private String toString(AssistHolder assistHolder, Transition t) {
        String nameOrLabel = t.getClass().getSimpleName();
        if (t instanceof AtomTransition) {
            nameOrLabel += ' ' + this.createLexer(assistHolder.content).getVocabulary().getDisplayName(((AtomTransition) t).label);
        }
        return nameOrLabel + " -> " + toString(t.target);
    }

    private String transitionsStr(AssistHolder assistHolder, ATNState state) {
        Stream<Transition> transitionsStream = Arrays.asList(state.getTransitions()).stream();
//        List<String> transitionStrings = transitionsStream.map(this::toString).collect(Collectors.toList());
        List<String> transitionStrings = transitionsStream.map(t -> toString(assistHolder, t)).collect(Collectors.toList());
        return StringUtils.collectionToDelimitedString(transitionStrings, ", ");
    }


    private Lexer createLexerWithUntokenizedTextDetection(AssistHolder assistHolder) {
        Lexer lexer = createLexer(assistHolder.content);
        lexer.removeErrorListeners();

        AssistErrorListener errorListener = new AssistErrorListener();
        assistHolder.errorListener = errorListener;
        lexer.addErrorListener(errorListener);


//        ANTLRErrorListener newErrorListener = new BaseErrorListener() {
//            @Override
//            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
//                    int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
//                untokenizedText = input.substring(charPositionInLine); // intended side effect
//            }
//        };
//        lexer.addErrorListener(newErrorListener);

        return lexer;
    }

//    private Lexer createLexer() {
//        return createLexer(this.input);
//    }

    private Lexer createLexer(String lexerInput) {
        return this.antlrFactory.createLexer(stringToCharStream(lexerInput));
    }

    private static List<? extends Token> filterOutNonDefaultChannels(List<? extends Token> tokens) {
        return tokens.stream().filter(t -> t.getChannel() == 0).collect(Collectors.toList());
    }

    private static CharStream stringToCharStream(String content) {
        try {
            return CharStreams.fromReader(new StringReader(content));
        } catch (IOException e) {
            throw new DslException( e);
        }
    }

    private static class AssistErrorListener extends BaseErrorListener {

    	Integer errorPosition = null;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
        	this.errorPosition = charPositionInLine;
//            untokenizedText = input.substring(charPositionInLine); // intended side effect
        }
    }

    private static class AssistHolder {

    	final String content;
    	AssistErrorListener errorListener;

		public AssistHolder(String content) {
			this.content = content;
		}

    }
}
