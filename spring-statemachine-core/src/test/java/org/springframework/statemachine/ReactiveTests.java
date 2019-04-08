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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));

		List<StateMachineEventResult<TestStates, TestEvents>> results = machine
				.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E1).build())).collect(Collectors.toList())
				.block();
		assertThat(results.size(), is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));

		results = machine
				.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E2).build())).collect(Collectors.toList())
				.block();
		assertThat(results.size(), is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S3));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));

		Flux<Message<TestEvents>> events = Flux.just(MessageBuilder.withPayload(TestEvents.E1).build(),
				MessageBuilder.withPayload(TestEvents.E2).build());
		List<StateMachineEventResult<TestStates, TestEvents>> results = machine.sendEvents(events).collect(Collectors.toList()).block();
		assertThat(results.size(), is(2));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S3));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test3() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));

		boolean accepted = machine.sendEvent(TestEvents.E1);
		assertThat(accepted, is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));

		accepted = machine.sendEvent(TestEvents.E2);
		assertThat(accepted, is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S3));

		accepted = machine.sendEvent(TestEvents.E3);
		assertThat(accepted, is(false));
	}

//	@Test
//	public void xxx1() {
//		//Flux.just("1", "2", "3")
//		Flux.just("1")
//			.scan((r,s) -> {
//				System.out.println("XXX1 " + r + " " + s);
//				return s + "X";
//			})
//			.doOnNext(r -> {
//				System.out.println("XXX2 " + r);
//			})
//			.subscribe();
//	}

	@Test
	public void xxx2() {
//		boolean a = true;
//		a &= false;
//		System.out.println(a);
//
//		a = true;
//		a &= true;
//		System.out.println(a);
//
//		a = true;
//		a &= false;
//		a &= true;
//		System.out.println(a);

		System.out.println("true&true = " + (true&true));
		System.out.println("true&false = " + (true&false));
		System.out.println("false&true = " + (false&true));
		System.out.println("false&false = " + (false&false));
		System.out.println("");

		System.out.println("true|true = " + (true|true));
		System.out.println("true|false = " + (true|false));
		System.out.println("false|true = " + (false|true));
		System.out.println("false|false = " + (false|false));
		System.out.println("");

		System.out.println("true^true = " + (true^true));
		System.out.println("true^false = " + (true^false));
		System.out.println("false^true = " + (false^true));
		System.out.println("false^false = " + (false^false));
	}

	@Test
	public void xxx3() {

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
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}
	}
}
