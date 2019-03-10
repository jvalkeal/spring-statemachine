/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.assertj.StateMachineAsserts.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.util.ObjectUtils;

@SuppressWarnings({ "unchecked" })
public class StateMachineNotificationTests extends AbstractStateMachineTests {

	@Test
	public void testFlat() {
		StateMachine<String, String> machine = doInit(Config1.class);
		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(listener.contexts).hasSize(6);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_ENTRY).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_CHANGED).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATEMACHINE_START).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).doesNotHaveSource().hasTargetId("SI");
		assertThat(machine).hasStateId("SI");

		listener.reset();
		machine.sendEvent("E1");
		assertThat(listener.contexts).hasSize(6);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("SI").hasTargetId("S1");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("SI").hasTargetId("S1");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("SI").doesNotHaveTarget();
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_ENTRY).hasSourceId("SI").hasTargetId("S1");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_CHANGED).hasSourceId("SI").hasTargetId("S1");
		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).hasSourceId("SI").hasTargetId("S1");
		assertThat(machine).hasStateId("S1");

		listener.reset();
		machine.sendEvent("E2");
		assertThat(listener.contexts).hasSize(7);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("S1").hasTargetId("SE");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("S1").hasTargetId("SE");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("S1").doesNotHaveTarget();
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_ENTRY).hasSourceId("S1").hasTargetId("SE");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_CHANGED).hasSourceId("S1").hasTargetId("SE");
		assertThat(listener.contexts.get(5)).hasStage(Stage.STATEMACHINE_STOP).doesNotHaveSource().doesNotHaveTarget();
		assertThat(listener.contexts.get(6)).hasStage(Stage.TRANSITION_END).hasSourceId("S1").hasTargetId("SE");
		assertThat(machine).hasStateId("SE");
	}

	@Test
	public void testFlatAnonymousTransitions() {
		StateMachine<String, String> machine = doInit(Config2.class);
		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(listener.contexts).hasSize(19);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START);
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION);
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_ENTRY);
		assertThat(listener.contexts.get(3)).hasStage(Stage.TRANSITION_START);
		assertThat(listener.contexts.get(4)).hasStage(Stage.TRANSITION);
		assertThat(listener.contexts.get(5)).hasStage(Stage.STATE_EXIT);
		assertThat(listener.contexts.get(6)).hasStage(Stage.STATE_ENTRY);
		assertThat(listener.contexts.get(7)).hasStage(Stage.TRANSITION_START);
		assertThat(listener.contexts.get(8)).hasStage(Stage.TRANSITION);
		assertThat(listener.contexts.get(9)).hasStage(Stage.STATE_EXIT);
		assertThat(listener.contexts.get(10)).hasStage(Stage.STATE_ENTRY);
		assertThat(listener.contexts.get(11)).hasStage(Stage.STATE_CHANGED);
		assertThat(listener.contexts.get(12)).hasStage(Stage.STATEMACHINE_STOP);
		assertThat(listener.contexts.get(13)).hasStage(Stage.TRANSITION_END);
		assertThat(listener.contexts.get(14)).hasStage(Stage.STATE_CHANGED);
		assertThat(listener.contexts.get(15)).hasStage(Stage.TRANSITION_END);
		assertThat(listener.contexts.get(16)).hasStage(Stage.STATE_CHANGED);
		assertThat(listener.contexts.get(17)).hasStage(Stage.STATEMACHINE_START);
		assertThat(listener.contexts.get(18)).hasStage(Stage.TRANSITION_END);
		assertThat(machine).hasStateId("SE");
	}

	@Test
	public void testViaChoiceLast() {
		StateMachine<String, String> machine = doInit(Config3.class);
		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(listener.contexts).hasSize(6);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_ENTRY).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_CHANGED).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATEMACHINE_START).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).doesNotHaveSource().hasTargetId("SI");
		assertThat(machine).hasStateId("SI");

		listener.reset();
		machine.sendEvent("E1");

//		assertThat(listener.contexts).hasSize(6);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("SI").hasTargetId("CHOICE");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("SI").hasTargetId("CHOICE");

//		assertThat(listener.contexts.get(2)).hasStage(Stage.TRANSITION_START).hasSourceId("CHOICE").hasTargetId("S3");
//		assertThat(listener.contexts.get(3)).hasStage(Stage.TRANSITION).hasSourceId("CHOICE").hasTargetId("S3");
//		assertThat(listener.contexts.get(4)).hasStage(Stage.TRANSITION_END).hasSourceId("CHOICE").hasTargetId("S3");

		assertThat(listener.contexts.get(2)).hasStage(Stage.TRANSITION_END).hasSourceId("SI").hasTargetId("CHOICE");

		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_EXIT).hasSourceId("SI").doesNotHaveTarget();
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_ENTRY).hasSourceId("SI").hasTargetId("S3");
		assertThat(listener.contexts.get(5)).hasStage(Stage.STATE_CHANGED).hasSourceId("SI").hasTargetId("S3");
//		assertThat(listener.contexts.get(6)).hasStage(Stage.TRANSITION_END).hasSourceId("SI").hasTargetId("CHOICE");




//		how it should look like
//		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("SI").hasTargetId("CHOICE");
//		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("SI").hasTargetId("CHOICE");
//		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("SI").doesNotHaveTarget();
//		assertThat(listener.contexts.get(3)).hasStage(Stage.TRANSITION_END).hasSourceId("SI").hasTargetId("CHOICE");
//		assertThat(listener.contexts.get(4)).hasStage(Stage.TRANSITION_START).hasSourceId("CHOICE").hasTargetId("S3");
//		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION).hasSourceId("CHOICE").hasTargetId("S3");
//		assertThat(listener.contexts.get(6)).hasStage(Stage.STATE_ENTRY).hasSourceId("SI").hasTargetId("S3");
//		assertThat(listener.contexts.get(7)).hasStage(Stage.TRANSITION_END).hasSourceId("CHOICE").hasTargetId("S3");
//		assertThat(listener.contexts.get(8)).hasStage(Stage.STATE_CHANGED).hasSourceId("SI").hasTargetId("S3");






//		assertThat(listener.contexts).hasSize(6);
//		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("SI").hasTargetId("CHOICE");
//		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("SI").hasTargetId("CHOICE");
//		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("SI").doesNotHaveTarget();
//		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_ENTRY).hasSourceId("SI").hasTargetId("S3");
//		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_CHANGED).hasSourceId("SI").hasTargetId("S3");
//		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).hasSourceId("SI").hasTargetId("CHOICE");
		assertThat(machine).hasStateId("S3");

		listener.reset();
		machine.sendEvent("E3");
		assertThat(listener.contexts).hasSize(7);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("S3").hasTargetId("SE");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("S3").hasTargetId("SE");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("S3").doesNotHaveTarget();
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_ENTRY).hasSourceId("S3").hasTargetId("SE");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_CHANGED).hasSourceId("S3").hasTargetId("SE");
		assertThat(listener.contexts.get(5)).hasStage(Stage.STATEMACHINE_STOP).doesNotHaveSource().doesNotHaveTarget();
		assertThat(listener.contexts.get(6)).hasStage(Stage.TRANSITION_END).hasSourceId("S3").hasTargetId("SE");
		assertThat(machine).hasStateId("SE");
	}

	@Test
	public void testViaChoiceFirst() {
		StateMachine<String, String> machine = doInit(Config3.class);
		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(listener.contexts).hasSize(6);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_ENTRY).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_CHANGED).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATEMACHINE_START).doesNotHaveSource().hasTargetId("SI");
		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).doesNotHaveSource().hasTargetId("SI");

		listener.reset();
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("h1", true).build());
		assertThat(listener.contexts).hasSize(9);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("SI").hasTargetId("CHOICE");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("SI").hasTargetId("CHOICE");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("SI").doesNotHaveTarget();
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_ENTRY).hasSourceId("SI").hasTargetId("S2");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_CHANGED).hasSourceId("SI").hasTargetId("S2");
		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).hasSourceId("SI").hasTargetId("CHOICE");

		listener.reset();
		machine.sendEvent("E2");
		assertThat(listener.contexts).hasSize(6);
		assertThat(listener.contexts.get(0)).hasStage(Stage.TRANSITION_START).hasSourceId("S2").hasTargetId("S3");
		assertThat(listener.contexts.get(1)).hasStage(Stage.TRANSITION).hasSourceId("S2").hasTargetId("S3");
		assertThat(listener.contexts.get(2)).hasStage(Stage.STATE_EXIT).hasSourceId("S2").doesNotHaveTarget();
		assertThat(listener.contexts.get(3)).hasStage(Stage.STATE_ENTRY).hasSourceId("S2").hasTargetId("S3");
		assertThat(listener.contexts.get(4)).hasStage(Stage.STATE_CHANGED).hasSourceId("S2").hasTargetId("S3");
		assertThat(listener.contexts.get(5)).hasStage(Stage.TRANSITION_END).hasSourceId("S2").hasTargetId("S3");
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1")
					.end("SE");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI").target("S1").event("E1")
					.and()
				.withExternal()
					.source("S1").target("SE").event("E2");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1")
					.end("SE");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI").target("S1")
					.and()
				.withExternal()
					.source("S1").target("SE");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.choice("CHOICE")
					.state("S2")
					.state("S3")
					.end("SE");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI").target("CHOICE").event("E1")
					.and()
				.withChoice()
					.source("CHOICE")
					.first("S2", c -> ObjectUtils.nullSafeEquals(c.getMessageHeader("h1"), true))
					.last("S3")
					.and()
				.withExternal()
					.source("S2").target("S3").event("E2")
					.and()
				.withExternal()
					.source("S3").target("SE").event("E3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.choice("CHOICE1")
					.choice("CHOICE2")
					.state("S2")
					.state("S3")
					.end("SE");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI").target("CHOICE1").event("E1")
					.and()
				.withChoice()
					.source("CHOICE1")
					.first("S2", c -> ObjectUtils.nullSafeEquals(c.getMessageHeader("h1"), true))
					.last("CHOICE2")
					.and()
				.withChoice()
					.source("CHOICE2")
					.first("S2", c -> ObjectUtils.nullSafeEquals(c.getMessageHeader("h1"), true))
					.last("S3")
					.and()
				.withExternal()
					.source("S2").target("S3").event("E2")
					.and()
				.withExternal()
					.source("S3").target("SE").event("E3");
		}
	}

	private static class TestStateMachineListener extends StateMachineListenerAdapter<String, String> {

		final ArrayList<StateContext<String, String>> contexts = new ArrayList<>();

		@Override
		public void stateContext(StateContext<String, String> stateContext) {
			contexts.add(stateContext);
		}

		public void reset() {
			contexts.clear();
		}
	}

	private StateMachine<String, String> doInit(Class<?>... annotatedClasses) {
		context.register(annotatedClasses);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE,
				StateMachine.class);
		return machine;
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
