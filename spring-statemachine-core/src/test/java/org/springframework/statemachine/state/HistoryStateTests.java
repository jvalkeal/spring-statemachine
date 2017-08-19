/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.statemachine.state;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;
import org.springframework.util.ObjectUtils;

public class HistoryStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testShallowInSubmachine() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testShallowNoHistoryDefaultsNormalEntry() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S20));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDeep() {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);
		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21, TestStates.S212));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testShallow() {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21, TestStates.S211));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDefaultNotEntered() {
		context.register(BaseConfig.class, Config4.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.EH);
		assertThat(machine.getState().getIds(), contains(TestStates.S3, TestStates.S33));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDefaultHistoryIsFinal() {
		context.register(BaseConfig.class, Config4.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S3, TestStates.S30));
		machine.sendEvent(TestEvents.EF);
		assertThat(machine.getState().getIds(), contains(TestStates.S3, TestStates.SF));
		machine.sendEvent(TestEvents.E4);
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.EH);
		assertThat(machine.getState().getIds(), contains(TestStates.S3, TestStates.S33));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.SHALLOW);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.DEEP)
						.and()
						.withStates()
							.parent(TestStates.S21)
							.initial(TestStates.S211)
							.state(TestStates.S211)
							.state(TestStates.S212);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S211)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S211)
					.target(TestStates.S212)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S212)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.SHALLOW)
						.and()
						.withStates()
							.parent(TestStates.S21)
							.initial(TestStates.S211)
							.state(TestStates.S211)
							.state(TestStates.S212);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S211)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S211)
					.target(TestStates.S212)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S212)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S3)
					.and()
					.withStates()
						.parent(TestStates.S3)
						.initial(TestStates.S30)
						.state(TestStates.S31)
						.state(TestStates.S32)
						.state(TestStates.S33)
						.end(TestStates.SF)
						.history(TestStates.SH, History.SHALLOW);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S3)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.S31)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S31)
					.target(TestStates.S32)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.SF)
					.event(TestEvents.EF)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S1)
					.event(TestEvents.E4)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.EH)
					.and()
				.withHistory()
					.source(TestStates.SH)
					.target(TestStates.S33);
		}
	}

	private static <S, E> State<S, E> findState(StateMachine<S, E> stateMachine, S id) {
		for (State<S, E> state1 : stateMachine.getStates()) {
			if (ObjectUtils.nullSafeEquals(state1.getId(), id)) {
				return state1;
			}
			if (state1.getStates() != null) {
				for (State<S, E> state2 : state1.getStates()) {
					if (ObjectUtils.nullSafeEquals(state2.getId(), id)) {
						return state2;
					}
				}

			}
		}
		return null;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testXXX1() {
		context.register(Config5.class);
		context.refresh();
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();

		State<String, String> stateHMAINDEEP = findState(machine, "HMAINDEEP");
		State<String, String> stateHS1DEEP = findState(machine, "HS1DEEP");
		assertThat(stateHMAINDEEP, notNullValue());
		assertThat(stateHS1DEEP, notNullValue());

		HistoryPseudoState<String, String> pseudoStateHMAINDEEP = (HistoryPseudoState<String, String>) stateHMAINDEEP.getPseudoState();
		HistoryPseudoState<String, String> pseudoStateHS1DEEP = (HistoryPseudoState<String, String>) stateHS1DEEP.getPseudoState();
		assertThat(pseudoStateHMAINDEEP, notNullValue());

		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), contains("S1", "S1I"));
	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1")
					.state("S2")
					.history("HMAINDEEP", History.DEEP)
//					.history("HMAINSHALLOW", History.SHALLOW)
					.and()
					.withStates()
						.parent("S1")
						.initial("S1I")
						.state("S10")
						.history("HS1DEEP", History.DEEP)
						.history("HS1SHALLOW", History.SHALLOW)
						.and()
						.withStates()
							.parent("S10")
							.initial("S100I")
							.state("S100")
							.history("HS10DEEP", History.DEEP)
							.history("HS10SHALLOW", History.SHALLOW)
							.and()
					.withStates()
						.parent("S2")
						.initial("S2I")
						.state("S20")
						.history("HS2DEEP", History.DEEP)
						.history("HS2SHALLOW", History.SHALLOW)
						.and()
						.withStates()
							.parent("S20")
							.initial("S200I")
							.state("S200")
							.history("HS20DEEP", History.DEEP)
							.history("HS20SHALLOW", History.SHALLOW);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1");
		}
	}

}
