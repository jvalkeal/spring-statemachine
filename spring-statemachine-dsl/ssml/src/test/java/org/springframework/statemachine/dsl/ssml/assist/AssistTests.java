package org.springframework.statemachine.dsl.ssml.assist;

import java.util.Collection;

import org.junit.Test;
import org.springframework.statemachine.dsl.ssml.SsmlLexer;
import org.springframework.statemachine.dsl.ssml.SsmlParser;
import org.springframework.util.StringUtils;

public class AssistTests {

	@Test
	public void testFoo() {
		ReflectionLexerAndParserFactory factory = new ReflectionLexerAndParserFactory(SsmlLexer.class, SsmlParser.class);

		String input = "";

        AutoSuggester suggester = new AutoSuggester(factory, input);
        suggester.setCasePreference(CasePreference.LOWER);
        Collection<String> suggestCompletions = suggester.suggestCompletions();
        System.out.println(StringUtils.collectionToCommaDelimitedString(suggestCompletions));

	}

}
