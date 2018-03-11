package org.springframework.statemachine.dsl.ssml.assist;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Collection;

import org.junit.Test;
import org.springframework.statemachine.dsl.antlr.assist.AntlrDslAssist;
import org.springframework.statemachine.dsl.ssml.SsmlAntlrFactory;

public class SsmlDslAssistTests {

	@Test
	public void testStateBlockKeywords() {
		SsmlAntlrFactory ssmlAntlrFactory = new SsmlAntlrFactory();

//		String input = "";
//		String input = "state S1{initial}state S2{}state S3{end}transition T1{source S1 target S2 event E1}transition T2{source S2 target S3 event E2}";
		String input = "state S1{";

        AntlrDslAssist suggester = new AntlrDslAssist(ssmlAntlrFactory);
        Collection<String> suggestCompletions = suggester.assistCompletions(input);
        suggestCompletions.stream().forEach(System.out::println);
		assertThat(suggestCompletions, containsInAnyOrder("entry", "exit", "initial", "guard", "action", "end", "do",
				"source", "event", "}", "bean", "target"));
	}

}
