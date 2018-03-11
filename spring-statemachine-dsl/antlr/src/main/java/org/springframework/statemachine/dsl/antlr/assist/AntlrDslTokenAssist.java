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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.AtomTransition;
import org.antlr.v4.runtime.atn.SetTransition;
import org.antlr.v4.runtime.atn.Transition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Given an ATN state and the lexer ATN, suggests auto-completion texts.
 */
class AntlrDslTokenAssist {

	private static final Log log = LogFactory.getLog(AntlrDslTokenAssist.class);
    private final Lexer lexer;
    private final Set<String> suggestions = new TreeSet<String>();
    private final List<Integer> visitedLexerStates = new ArrayList<>();
    private String origPartialToken;

    public AntlrDslTokenAssist(Lexer lexer) {
        this.lexer = lexer;
    }


    public Collection<String> suggest(Collection<Integer> nextParserTransitionLabels, String remainingText) {
//        logTokensUsedForSuggestion(nextParserTransitionLabels);
        this.origPartialToken = remainingText;
        for (int nextParserTransitionLabel : nextParserTransitionLabels) {
            int nextTokenRuleNumber = nextParserTransitionLabel - 1; // Count from 0 not from 1
            ATNState lexerState = findLexerStateByRuleNumber(nextTokenRuleNumber);
            suggest("", lexerState, remainingText);
        }
        return suggestions;
    }

//    private void logTokensUsedForSuggestion(Collection<Integer> ruleIndices) {
//        if (!log.isDebugEnabled()) {
//            return;
//        }
//        String ruleNames = ruleIndices.stream().map(r -> lexer.getRuleNames()[r]).collect(Collectors.joining(" "));
//        log.debug("Suggesting tokens for lexer rules: " + ruleNames);
//    }

    private ATNState findLexerStateByRuleNumber(int ruleNumber) {
        return lexer.getATN().ruleToStartState[ruleNumber];
    }

    private void suggest(String tokenSoFar, ATNState lexerState, String remainingText) {
        log.debug(
                "SUGGEST: tokenSoFar=" + tokenSoFar + " remainingText=" + remainingText + " lexerState=" + toString(lexerState));
        if (visitedLexerStates.contains(lexerState.stateNumber)) {
            return; // avoid infinite loop and stack overflow
        }
        visitedLexerStates.add(lexerState.stateNumber);
        try {
            Transition[] transitions = lexerState.getTransitions();
            boolean tokenNotEmpty = tokenSoFar.length() > 0;
            boolean noMoreCharactersInToken = (transitions.length == 0);
            if (tokenNotEmpty && noMoreCharactersInToken) {
                addSuggestedToken(tokenSoFar);
                return;
            }
            for (Transition trans : transitions) {
                suggestViaLexerTransition(tokenSoFar, remainingText, trans);
            }
        } finally {
            visitedLexerStates.remove(visitedLexerStates.size() - 1);
        }
    }

    private String toString(ATNState lexerState) {
        String ruleName = this.lexer.getRuleNames()[lexerState.ruleIndex];
        return ruleName + " " + lexerState.getClass().getSimpleName() + " " + lexerState;
    }

    private void suggestViaLexerTransition(String tokenSoFar, String remainingText, Transition trans) {
        if (trans.isEpsilon()) {
            suggest(tokenSoFar, trans.target, remainingText);
        } else if (trans instanceof AtomTransition) {
            String newTokenChar = getAddedTextFor((AtomTransition) trans);
            if (remainingText.isEmpty() || remainingText.startsWith(newTokenChar)) {
                log.debug("LEXER TOKEN: " + newTokenChar + " remaining=" + remainingText);
                suggestViaNonEpsilonLexerTransition(tokenSoFar, remainingText, newTokenChar, trans.target);
            } else {
                log.debug("NONMATCHING LEXER TOKEN: " + newTokenChar + " remaining=" + remainingText);
            }
        } else if (trans instanceof SetTransition) {
            List<Integer> symbols = ((SetTransition) trans).label().toList();
            for (Integer symbol : symbols) {
                char[] charArr = Character.toChars(symbol);
                String charStr = new String(charArr);
                boolean shouldIgnoreCase = shouldIgnoreThisCase(charArr[0], symbols); // TODO: check for non-BMP
                if (!shouldIgnoreCase && (remainingText.isEmpty() || remainingText.startsWith(charStr))) {
                    suggestViaNonEpsilonLexerTransition(tokenSoFar, remainingText, charStr, trans.target);
                }
            }
        }
    }

    private void suggestViaNonEpsilonLexerTransition(String tokenSoFar, String remainingText,
            String newTokenChar, ATNState targetState) {
        String newRemainingText = (remainingText.length() > 0) ? remainingText.substring(1) : remainingText;
        suggest(tokenSoFar + newTokenChar, targetState, newRemainingText);
    }

    private void addSuggestedToken(String tokenToAdd) {
        String justTheCompletionPart = chopOffCommonStart(tokenToAdd, this.origPartialToken);
        suggestions.add(justTheCompletionPart);
    }

    private String chopOffCommonStart(String a, String b) {
        int charsToChopOff = Math.min(b.length(), a.length());
        return a.substring(charsToChopOff);
    }

    private String getAddedTextFor(AtomTransition transition) {
        return new String(Character.toChars(transition.label));
    }

    private boolean shouldIgnoreThisCase(char transChar, List<Integer> allTransChars) {
    	return Character.isUpperCase(transChar) && allTransChars.contains((int)Character.toLowerCase(transChar));
    }
}
