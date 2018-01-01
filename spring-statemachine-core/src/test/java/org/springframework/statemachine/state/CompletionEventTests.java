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
package org.springframework.statemachine.state;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class CompletionEventTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSimpleStateCompletes() throws Exception {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestCountAction testAction2 = context.getBean("testAction2", TestCountAction.class);

		machine.start();

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
//		assertThat(machine.getState().getId(), is(TestStates.S2));

		assertThat(testAction2.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction2.count, is(1));
		assertThat(machine.getState().getId(), is(TestStates.S3));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.stateDo(TestStates.S2, testAction2())
					.state(TestStates.S3);
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
					.source(TestStates.S2)
					.target(TestStates.S3);
		}

		@Bean
		public TestCountAction testAction2() {
			return new TestCountAction() {
				@Override
				public void execute(StateContext<TestStates, TestEvents> context) {
					System.out.println("EXE 1");
					for (int i = 0; i < 10; i++) {
						System.out.println("EXE 2 " + i);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							System.out.println("EXEI 2 " + i);
						}
					}
					super.execute(context);
					System.out.println("EXE 3");
				}
			};
		}
	}

	private static class TestCountAction implements Action<TestStates, TestEvents> {

		int count = 0;
		StateContext<TestStates, TestEvents> context;
		CountDownLatch latch = new CountDownLatch(1);

		public TestCountAction() {
			count = 0;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			this.context = context;
			count++;
			latch.countDown();
		}

	}

}
