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
package org.springframework.statemachine.dsl.ssml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.dsl.DslParserResult;
import org.springframework.util.ObjectUtils;

/**
 * Tests for {@link SsmlDslParser}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlDslParserTests {

	@Test
	public void testSimpleMachineNormal() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-normal.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineMixedorder() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-mixedorder.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineErrorWrongState() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-error-wrongstate.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(1));
		assertThat(dslParserResult.hasErrors(), is(true));
		assertThat(dslParserResult.getErrors().get(0).getMessage(), is("undefined state 'S4' referenced in transition target"));
		assertThat(dslParserResult.getErrors().get(0).getLine(), is(22));
		assertThat(dslParserResult.getErrors().get(0).getPosition(), is(9));
	}

	@Test
	public void testSimpleMachineErrorLexical() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-error-lexical.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(1));
		assertThat(dslParserResult.hasErrors(), is(true));
		assertThat(dslParserResult.getErrors().get(0).getMessage(), is("missing '}' at 'state'"));
		assertThat(dslParserResult.getErrors().get(0).getLine(), is(7));
		assertThat(dslParserResult.getErrors().get(0).getPosition(), is(0));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineOneliner() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-oneliner.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	private static void assertSimpleMachineModel(DslParserResult<StateMachineModel<String, String>> dslParserResult) {
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getModel(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData().getStateData().size(), is(3));
		assertThat(dslParserResult.getModel().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getModel().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getModel().getTransitionsData().getTransitions().size(), is(2));

		Collection<StateData<String, String>> stateData = dslParserResult.getModel().getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitions = dslParserResult.getModel().getTransitionsData()
				.getTransitions();

		assertThat(stateData.stream().map(sd -> sd.getState()).collect(Collectors.toList()),
				containsInAnyOrder("S1", "S2", "S3"));


		assertThat(transitions.stream().map(t -> t.getSource() + t.getTarget()).collect(Collectors.toList()),
				containsInAnyOrder("S1S2", "S2S3"));

		stateData.stream().forEach(sd -> {
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S1")) {
				assertThat(sd.isInitial(), is(true));
				assertThat(sd.isEnd(), is(false));
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S2")) {
				assertThat(sd.isInitial(), is(false));
				assertThat(sd.isEnd(), is(false));
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S3")) {
				assertThat(sd.isInitial(), is(false));
				assertThat(sd.isEnd(), is(true));
			}
		});

		transitions.stream().forEach(t -> {
			if (ObjectUtils.nullSafeEquals(t.getSource(), "S1")) {
				assertThat(t.getEvent(), is("E1"));
			}
			if (ObjectUtils.nullSafeEquals(t.getSource(), "S2")) {
				assertThat(t.getEvent(), is("E2"));
			}
		});
	}
}
