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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BaseErrorListener;
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
import org.springframework.statemachine.dsl.DslAssist;
import org.springframework.statemachine.dsl.antlr.AntlrFactory;
import org.springframework.util.StringUtils;

/**
 * A {@link DslAssist} implementation which can be used as is to provide assist
 * solely based on {@code ANTLR} grammar. Further customisations can be provided
 * by subclass implementations.
 *
 * @author Janne Valkealahti
 *
 */
public class AntlrDslAssist extends AbstractAntlrDslAssist {

	private static final Log log = LogFactory.getLog(AntlrDslAssist.class);

    /**
     * Instantiate a new Antlr Dsl Assist.
     *
     * @param antlrFactory the Antlr Factory
     */
    public AntlrDslAssist(AntlrFactory antlrFactory) {
    	super(antlrFactory);
    }

	@Override
	public Collection<String> assistCompletions(String content) {
		AssistHolder assistHolder = new AssistHolder(content);
        tokenizeInput(assistHolder);
        storeParserAtnAndRuleNames(assistHolder);
        runParserAtnAndCollectSuggestions(assistHolder);
        return assistHolder.collectedSuggestions;
    }

    private void tokenizeInput(AssistHolder assistHolder) {
        Lexer lexer = createLexerWithUntokenizedTextDetection(assistHolder);
        List<? extends Token> allTokens = lexer.getAllTokens();
        assistHolder.inputTokens = filterTokensByChannel(allTokens, 0);
    }

    private void storeParserAtnAndRuleNames(AssistHolder assistHolder) {
        Parser parserForAtnOnly = createParser();
        assistHolder.parserAtn = parserForAtnOnly.getATN();
        assistHolder.parserRuleNames = parserForAtnOnly.getRuleNames();
    }

    private void runParserAtnAndCollectSuggestions(AssistHolder assistHolder) {
        ATNState initialState = assistHolder.parserAtn.states.get(0);
        log.debug("Parser initial state: " + initialState);
        parseAndCollectTokenSuggestions(assistHolder, initialState, 0);
    }

    /**
     * Recursive through the parser ATN to process all tokens. When successful (out of tokens) - collect completion
     * suggestions.
     */
    private void parseAndCollectTokenSuggestions(AssistHolder assistHolder, ATNState parserState, int tokenListIndex) {
    	assistHolder.indent = assistHolder.indent + "  ";
        try {
            log.debug(assistHolder.indent + "State: " + toString(assistHolder, parserState) );
            log.debug(assistHolder.indent + "State available transitions: " + transitionsStr(assistHolder, parserState));

            if (!haveMoreTokens(assistHolder, tokenListIndex)) { // stop condition for recursion
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
        	assistHolder.indent = assistHolder.indent.substring(2);
        }
    }

    private boolean haveMoreTokens(AssistHolder assistHolder, int tokenListIndex) {
        return tokenListIndex < assistHolder.inputTokens.size();
    }

    private void handleEpsilonTransition(AssistHolder assistHolder, Transition trans, int tokenListIndex) {
        // Epsilon transitions don't consume a token, so don't move the index
        parseAndCollectTokenSuggestions(assistHolder, trans.target, tokenListIndex);
    }

    private void handleAtomicTransition(AssistHolder assistHolder, AtomTransition trans, int tokenListIndex) {
        Token nextToken = assistHolder.inputTokens.get(tokenListIndex);
        int nextTokenType = assistHolder.inputTokens.get(tokenListIndex).getType();
        boolean nextTokenMatchesTransition = (trans.label == nextTokenType);
        if (nextTokenMatchesTransition) {
            log.debug(assistHolder.indent + "Token " + nextToken + " following transition: " + toString(assistHolder, trans));
            parseAndCollectTokenSuggestions(assistHolder, trans.target, tokenListIndex + 1);
        } else {
            log.debug(assistHolder.indent + "Token " + nextToken + " NOT following transition: " + toString(assistHolder, trans));
        }
    }

    private void handleSetTransition(AssistHolder assistHolder, SetTransition trans, int tokenListIndex) {
        Token nextToken = assistHolder.inputTokens.get(tokenListIndex);
        int nextTokenType = nextToken.getType();
        for (int transitionTokenType : trans.label().toList()) {
            boolean nextTokenMatchesTransition = (transitionTokenType == nextTokenType);
            if (nextTokenMatchesTransition) {
                log.debug(assistHolder.indent + "Token " + nextToken + " following transition: " + toString(assistHolder, trans) + " to " + transitionTokenType);
                parseAndCollectTokenSuggestions(assistHolder, trans.target, tokenListIndex + 1);
            } else {
                log.debug(assistHolder.indent + "Token " + nextToken + " NOT following transition: " + toString(assistHolder, trans) + " to " + transitionTokenType);
            }
        }
    }

    private void suggestNextTokensForParserState(AssistHolder assistHolder, ATNState parserState) {
        Set<Integer> transitionLabels = new HashSet<>();
        fillParserTransitionLabels(assistHolder, parserState, transitionLabels, new HashSet<>());
        AntlrDslTokenAssist tokenSuggester = new AntlrDslTokenAssist(createLexer(assistHolder.content));
        String untokenizedText = assistHolder.errorListener.errorPosition != null ? assistHolder.content.substring(assistHolder.errorListener.errorPosition) : "";
        Collection<String> suggestions = tokenSuggester.suggest(transitionLabels, untokenizedText);
        parseSuggestionsAndAddValidOnes(assistHolder, parserState, suggestions);
        log.debug(assistHolder.indent + "WILL SUGGEST TOKENS FOR STATE: " + parserState);
    }

    private void fillParserTransitionLabels(AssistHolder assistHolder, ATNState parserState, Collection<Integer> result, Set<TransitionHolder> visitedTransitions) {
        for (Transition trans : parserState.getTransitions()) {
            TransitionHolder transWrapper = new TransitionHolder(parserState, trans);
            if (visitedTransitions.contains(transWrapper)) {
                log.debug(assistHolder.indent + "Not following visited " + transWrapper);
                continue;
            }
            if (trans.isEpsilon()) {
                try {
                    visitedTransitions.add(transWrapper);
                    fillParserTransitionLabels(assistHolder, trans.target, result, visitedTransitions);
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
            	assistHolder.collectedSuggestions.add(suggestion);
            } else {
                log.debug("DROPPING non-parseable suggestion: " + suggestion);
            }
        }
    }

    private Token getAddedToken(AssistHolder assistHolder, String suggestedCompletion) {
        String completedText = assistHolder.content + suggestedCompletion;
        Lexer completedTextLexer = this.createLexer(completedText);
        completedTextLexer.removeErrorListeners();
        List<? extends Token> completedTextTokens = filterTokensByChannel(completedTextLexer.getAllTokens(), 0);
        if (completedTextTokens.size() <= assistHolder.inputTokens.size()) {
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

    private String toString(AssistHolder assistHolder, ATNState parserState) {
        String ruleName = assistHolder.parserRuleNames[parserState.ruleIndex];
        return ruleName + " " + parserState.getClass().getSimpleName() + " " + parserState;
    }

    private String toString(AssistHolder assistHolder, Transition t) {
        String nameOrLabel = t.getClass().getSimpleName();
        if (t instanceof AtomTransition) {
            nameOrLabel += ' ' + this.createLexer(assistHolder.content).getVocabulary().getDisplayName(((AtomTransition) t).label);
        }
        return nameOrLabel + " -> " + toString(assistHolder, t.target);
    }

    private String transitionsStr(AssistHolder assistHolder, ATNState state) {
        Stream<Transition> transitionsStream = Arrays.asList(state.getTransitions()).stream();
        List<String> transitionStrings = transitionsStream.map(t -> toString(assistHolder, t)).collect(Collectors.toList());
        return StringUtils.collectionToDelimitedString(transitionStrings, ", ");
    }

    private Lexer createLexerWithUntokenizedTextDetection(AssistHolder assistHolder) {
        Lexer lexer = createLexer(assistHolder.content);
        lexer.removeErrorListeners();

        AssistErrorListener errorListener = new AssistErrorListener();
        assistHolder.errorListener = errorListener;
        lexer.addErrorListener(errorListener);

        return lexer;
    }

    private static class AssistErrorListener extends BaseErrorListener {

    	Integer errorPosition = null;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
        	this.errorPosition = charPositionInLine;
        }
    }

    private static class AssistHolder {

    	final String content;
    	final Set<String> collectedSuggestions = new HashSet<>();
    	AssistErrorListener errorListener;
    	private List<? extends Token> inputTokens;
    	ATN parserAtn;
    	String[] parserRuleNames;
    	String indent = "";

		public AssistHolder(String content) {
			this.content = content;
		}
    }

	public static class TransitionHolder {
		private final ATNState source;
		private final Transition transition;

		public TransitionHolder(ATNState source, Transition transition) {
			super();
			this.source = source;
			this.transition = transition;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((transition == null) ? 0 : transition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TransitionHolder other = (TransitionHolder) obj;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (transition == null) {
				if (other.transition != null)
					return false;
			} else if (!transition.equals(other.transition))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return transition.getClass().getSimpleName() + " from " + source + " to " + transition.target;
		}
	}
}
