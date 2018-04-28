package org.springframework.statemachine.dsl.ssml.assist;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

public class SsmlDslAssistTests {

	@Test
	public void testStateBlockKeywords() {
//		String input = "state S1{initial}state S2{}state S3{end}transition T1{source S1 target S2 event E1}transition T2{source S2 target S3 event E2}";
		String input = "state S1{";

//        DslAssist suggester = new SsmlAntlrDslAssist();
//        Collection<String> suggestCompletions = suggester.assistCompletions(input);
//        suggestCompletions.stream().forEach(System.out::println);
//		assertThat(suggestCompletions, containsInAnyOrder("entry", "exit", "initial", "guard", "action", "end", "do",
//				"source", "event", "}", "bean", "target"));
	}

	@Test
	public void testStateBlockKeywords2() {
		String input = "state S1{initial}state S2{}state S3{end}transition T1{source";

//        DslAssist suggester = new SsmlAntlrDslAssist();
//        Collection<String> suggestCompletions = suggester.assistCompletions(input);
//        suggestCompletions.stream().forEach(System.out::println);
//		assertThat(suggestCompletions, containsInAnyOrder("S1", "S2", "S3"));
	}

}
