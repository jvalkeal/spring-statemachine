/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.persist;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;

public class StateMachinePersistTests5 extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTimer1() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestStateMachineListener1 listener1 = new TestStateMachineListener1();
		stateMachine.addStateListener(listener1);
		stateMachine.start();
		assertThat(listener1.internalTransitionLatch.await(2, TimeUnit.SECONDS), is(false));
		stateMachine.sendEvent("E1");
		assertThat(listener1.internalTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), contains("S2"));

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		persister.restore(stateMachine, "xxx");
		TestStateMachineListener1 listener2 = new TestStateMachineListener1();
		stateMachine.addStateListener(listener2);
		assertThat(stateMachine.getState().getIds(), contains("S2"));
		assertThat(listener2.internalTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTimer2() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestStateMachineListener1 listener1 = new TestStateMachineListener1();
		stateMachine.addStateListener(listener1);
		stateMachine.start();
		assertThat(listener1.internalTransitionLatch.await(2, TimeUnit.SECONDS), is(false));
		stateMachine.sendEvent("E1");
		assertThat(listener1.internalTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), contains("S2"));

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		persister.restore(stateMachine, "xxx");
		TestStateMachineListener1 listener2 = new TestStateMachineListener1();
		stateMachine.addStateListener(listener2);
		assertThat(stateMachine.getState().getIds(), contains("S2"));
		assertThat(listener2.internalTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withInternal()
					.source("S2")
					.timer(1000);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withInternal()
					.source("S2")
					.timerOnce(1000);
		}
	}

	static class InMemoryStateMachinePersist1 implements StateMachinePersist<String, String, String> {

		private final HashMap<String, StateMachineContext<String, String>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<String, String> context, String contextObj) throws Exception {
			contexts.put(contextObj, context);
		}

		@Override
		public StateMachineContext<String, String> read(String contextObj) throws Exception {
			return contexts.get(contextObj);
		}
	}

	public static class TestStateMachineListener1 extends StateMachineListenerAdapter<String, String> {

		public volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		public volatile CountDownLatch internalTransitionLatch = new CountDownLatch(1);

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<String, String> transition) {
			System.out.println("            HI   ");
			if (transition.getKind() == TransitionKind.INTERNAL) {
				internalTransitionLatch.countDown();
			}
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			internalTransitionLatch = new CountDownLatch(c2);
		}

	}
}
