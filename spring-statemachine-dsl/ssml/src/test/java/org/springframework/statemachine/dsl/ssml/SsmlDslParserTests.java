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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dsl.DslParserResult;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.dsl.ssml.support.SsmlDslParser;
import org.springframework.statemachine.guard.Guard;
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
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineNotransitionids() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-notransitionids.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineNormalWithEnums() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-normal.ssml");
		StateMachineComponentResolver<MyStates, MyEvents> resolver = new MockStateMachineComponentResolver2();

		SsmlDslParser<MyStates, MyEvents> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> MyStates.valueOf(id));
		ssmlDslParser.setEventMapperFunction(id -> MyEvents.valueOf(id));
		DslParserResult<StateMachineModel<MyStates, MyEvents>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModelWithEnums(dslParserResult);
	}

	@Test
	public void testSimpleMachineMixedorder() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-mixedorder.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineErrorWrongState() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-error-wrongstate.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(1));
		assertThat(dslParserResult.hasErrors(), is(true));
		assertThat(dslParserResult.getErrors().get(0).getMessage(), is("undefined state 'S4' referenced in transition target"));

		assertThat(dslParserResult.getErrors().get(0).getRange().getStart().getLine(), is(21));
		assertThat(dslParserResult.getErrors().get(0).getRange().getStart().getCharacter(), is(9));
	}

	@Test
	public void testSimpleMachineErrorLexical() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-error-lexical.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(1));
		assertThat(dslParserResult.hasErrors(), is(true));
		assertThat(dslParserResult.getErrors().get(0).getMessage(), is("missing '}' at 'state'"));

		assertThat(dslParserResult.getErrors().get(0).getRange().getStart().getLine(), is(7));
		assertThat(dslParserResult.getErrors().get(0).getRange().getStart().getCharacter(), is(0));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testSimpleMachineOneliner() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-oneliner.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSimpleMachineModel(dslParserResult);
	}

	@Test
	public void testActionGuardMachineNormal() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/actionguardmachine-normal.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertActionGuardMachineModel(dslParserResult);
	}

	@Test
	public void testSubMachineNormal() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/submachine-normal.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertSubMachineModel(dslParserResult);
	}

	@Test
	public void testTransitionMachineNotransitionids() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/transitionmachine-notransitionids.ssml");
		StateMachineComponentResolver<String, String> resolver = new MockStateMachineComponentResolver();

		SsmlDslParser<String, String> ssmlDslParser = new SsmlDslParser<>(resolver);
		ssmlDslParser.setStateMapperFunction(id -> id);
		ssmlDslParser.setEventMapperFunction(id -> id);
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));

		assertTransitionMachineModel(dslParserResult);
	}

	private static void assertSimpleMachineModel(DslParserResult<StateMachineModel<String, String>> dslParserResult) {
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getResult(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData().size(), is(3));
		assertThat(dslParserResult.getResult().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions().size(), is(2));

		Collection<StateData<String, String>> stateData = dslParserResult.getResult().getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitions = dslParserResult.getResult().getTransitionsData()
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

	private static void assertSimpleMachineModelWithEnums(DslParserResult<StateMachineModel<MyStates, MyEvents>> dslParserResult) {
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getResult(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData().size(), is(3));
		assertThat(dslParserResult.getResult().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions().size(), is(2));

		Collection<StateData<MyStates, MyEvents>> stateData = dslParserResult.getResult().getStatesData().getStateData();
		Collection<TransitionData<MyStates, MyEvents>> transitions = dslParserResult.getResult().getTransitionsData()
				.getTransitions();

		assertThat(stateData.stream().map(sd -> sd.getState()).collect(Collectors.toList()),
				containsInAnyOrder(MyStates.S1, MyStates.S2, MyStates.S3));

		assertThat(transitions.stream().map(t -> t.getSource().toString() + t.getTarget().toString())
				.collect(Collectors.toList()), containsInAnyOrder("S1S2", "S2S3"));

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
			if (ObjectUtils.nullSafeEquals(t.getSource(), MyStates.S1)) {
				assertThat(t.getEvent(), is(MyEvents.E1));
			}
			if (ObjectUtils.nullSafeEquals(t.getSource(), MyStates.S2)) {
				assertThat(t.getEvent(), is(MyEvents.E2));
			}
		});
	}

	private static void assertTransitionMachineModel(DslParserResult<StateMachineModel<String, String>> dslParserResult) {
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getResult(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData().size(), is(3));
		assertThat(dslParserResult.getResult().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions().size(), is(6));

		Collection<StateData<String, String>> stateData = dslParserResult.getResult().getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitions = dslParserResult.getResult().getTransitionsData()
				.getTransitions();

		assertThat(stateData.stream().map(sd -> sd.getState()).collect(Collectors.toList()),
				containsInAnyOrder("S1", "S2", "S3"));

		assertThat(transitions.stream().map(t -> t.getSource() + t.getTarget()).collect(Collectors.toList()),
				containsInAnyOrder("S1S2", "S2S3", "S1S3", "S2S1", "S2null", "S1S1"));

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
//			if (ObjectUtils.nullSafeEquals(t.getSource(), "S1")) {
//				assertThat(t.getEvent(), is("E1"));
//			}
//			if (ObjectUtils.nullSafeEquals(t.getSource(), "S2")) {
//				assertThat(t.getEvent(), is("E2"));
//			}
		});
	}

	private static void assertActionGuardMachineModel(DslParserResult<StateMachineModel<String, String>> dslParserResult) {
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getResult(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData().size(), is(3));

		Collection<StateData<String, String>> stateData = dslParserResult.getResult().getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitions = dslParserResult.getResult().getTransitionsData()
				.getTransitions();

		stateData.stream().forEach(sd -> {
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S1")) {
				assertThat(sd.getExitActions(), notNullValue());
				assertThat(sd.getExitActions().size(), is(1));
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S2")) {
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S3")) {
			}
		});

		assertThat(dslParserResult.getResult().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions().size(), is(2));

		transitions.stream().forEach(t -> {
			if (ObjectUtils.nullSafeEquals(t.getSource(), "S1")) {
				assertThat(t.getEvent(), is("E1"));
				assertThat(t.getGuard(), nullValue());
			}
			if (ObjectUtils.nullSafeEquals(t.getSource(), "S2")) {
				assertThat(t.getEvent(), is("E2"));
				assertThat(t.getGuard(), notNullValue());
			}
		});
	}

	private static void assertSubMachineModel(DslParserResult<StateMachineModel<String, String>> dslParserResult) {
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getResult(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getResult().getStatesData().getStateData().size(), is(5));
		assertThat(dslParserResult.getResult().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getResult().getTransitionsData().getTransitions().size(), is(2));

		Collection<StateData<String, String>> stateData = dslParserResult.getResult().getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitions = dslParserResult.getResult().getTransitionsData()
				.getTransitions();

		assertThat(stateData.stream().map(sd -> sd.getState()).collect(Collectors.toList()),
				containsInAnyOrder("S1", "S2", "S3", "S21", "S22"));

		assertThat(transitions.stream().map(t -> t.getSource() + t.getTarget()).collect(Collectors.toList()),
				containsInAnyOrder("S1S2", "S2S3"));

		stateData.stream().forEach(sd -> {
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S1")) {
				assertThat(sd.isInitial(), is(true));
				assertThat(sd.isEnd(), is(false));
				assertThat(sd.getParent(), nullValue());
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S2")) {
				assertThat(sd.isInitial(), is(false));
				assertThat(sd.isEnd(), is(false));
				assertThat(sd.getParent(), nullValue());
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S3")) {
				assertThat(sd.isInitial(), is(false));
				assertThat(sd.isEnd(), is(true));
				assertThat(sd.getParent(), nullValue());
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S21")) {
				assertThat(sd.isInitial(), is(true));
				assertThat(sd.isEnd(), is(false));
				assertThat(sd.getParent(), is("S2"));
			}
			if (ObjectUtils.nullSafeEquals(sd.getState(), "S22")) {
				assertThat(sd.isInitial(), is(false));
				assertThat(sd.isEnd(), is(false));
				assertThat(sd.getParent(), is("S2"));
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

	private static class MockStateMachineComponentResolver implements StateMachineComponentResolver<String, String> {

		@Override
		public Action<String, String> resolveAction(String id) {
			return (ctx) -> {};
		}

		@Override
		public Guard<String, String> resolveGuard(String id) {
			return (ctx) -> true;
		}
	}

	enum MyStates {
		S1,S2,S3;
	}

	enum MyEvents {
		E1,E2;
	}

	private static class MockStateMachineComponentResolver2 implements StateMachineComponentResolver<MyStates, MyEvents> {

		@Override
		public Action<MyStates, MyEvents> resolveAction(String id) {
			return (ctx) -> {};
		}

		@Override
		public Guard<MyStates, MyEvents> resolveGuard(String id) {
			return (ctx) -> true;
		}
	}
}
