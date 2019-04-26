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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.awaitility.Awaitility;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

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

		StepVerifier.create(machine.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E1).build())))
			.expectNextMatches(r -> {
				return r.getResultType() == ResultType.ACCEPTED;
			})
			.expectComplete()
			.verify();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));

		StepVerifier.create(machine.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E2).build())))
			.expectNextMatches(r -> {
				return r.getResultType() == ResultType.ACCEPTED;
			})
			.expectComplete()
			.verify();
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


	@Test
	@SuppressWarnings("unchecked")
	public void testJoin() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.SI));

//		machine.sendEvent(TestEvents.E1);
//		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));

		StepVerifier.create(machine.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E1).build())))
			.expectNextMatches(r -> {
				return r.getResultType() == ResultType.ACCEPTED;
			})
			.expectComplete()
			.verify();
		Awaitility
			.await()
			.until(() -> machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));

//		StepVerifier.create(machine.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E2).build())))
//			.expectNextMatches(r -> {
//				return r.getResultType() == ResultType.ACCEPTED;
//			})
//			.expectComplete()
//			.verify();
//		Awaitility
//			.await()
//			.until(() -> machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S30));
//
//		StepVerifier.create(machine.sendEvent(Mono.just(MessageBuilder.withPayload(TestEvents.E3).build())))
//			.expectNextMatches(r -> {
//				return r.getResultType() == ResultType.ACCEPTED;
//			})
//			.expectComplete()
//			.verify();
//		Awaitility
//			.await()
//			.until(() -> machine.getState().getIds(), containsInAnyOrder(TestStates.S4));
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

//	@Test
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

//	@Test
	public void xxx3() {
		Mono<Void> mono1 = Mono.defer(() -> {
			System.out.println("hi1");
			return Mono.empty();
		});
		Mono<Void> mono2 = Mono.defer(() -> {
			System.out.println("hi2");
			return Mono.empty();
		});
		mono1.and(mono2).block();
	}

	private boolean test;

//	@Test
	public void xxx4() {
		Flux.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
			.groupBy(i -> i % 2 == 0)
			.flatMap(group -> group
				.flatMap(ReactiveTests::check)
				.doOnNext(System.out::println)
				.count()
				.doOnNext(c -> {
					if (group.key()) {
						System.out.println("total evens " + c);
					} else {
						System.out.println("total odds " + c);
					}
				}))
			.blockLast();
	}

	private static Mono<String> check(int i) {
		return Mono.fromSupplier(() -> {
			if (i % 2 == 0) {
				return i + " is even";
			} else {
				return i + " is odd";
			}
		}).subscribeOn(Schedulers.parallel());
	}

//	@Test
	public void xxx5() {
		Flux<Mono<Boolean>> randomMonoFlux = Flux.generate((sink) -> {
			sink.next(random());
		});

		Flux<Boolean> randomFlux = Flux.concat(randomMonoFlux);

		randomFlux.doOnNext(b -> {
			System.out.println("XXX2 "+ b);
		}).takeUntil(b -> b).blockLast();
	}

//	@Test
	public void xxx6() {
		Mono<String> mono1 = Mono.just("HI 1").doOnNext(System.out::println);
		Mono<String> mono2 = Mono.just("HI 2").doOnNext(System.out::println);
		Mono<String> mono3 = Mono.just("HI 3").doOnNext(System.out::println);
		Mono.empty().and(mono1).and(mono2).and(mono3).block();
	}

//	@Test
	public void xxx7() {
		Flux<String> flux = Flux.generate(
			() -> 0,
			(state, sink) -> {
				sink.next("3 x " + state + " = " + 3 * state);
				if (state == 10)
					sink.complete();
				return state + 1;
		});
	}

	private Mono<Boolean> random() {
		return Mono.defer(() -> {
			Random r = new Random();
			return Mono.just(r.nextInt(10)).doOnNext(i -> {
				System.out.println("XXX1 "+ i);
			}).map(i -> i > 8);
		});
	}

//	@Test
	public void xxx8() {
		EmitterProcessor<String> xxx1 = EmitterProcessor.<String>create(false);
		Flux<String> xxx2 = xxx1.cache(1);
		System.out.println("Post start1");
		xxx1.onNext("start1");
		System.out.println("flux block");
		xxx2.doOnNext(System.out::println).next().block();
		System.out.println("Post stop1");
		xxx1.onNext("stop1");
		System.out.println("flux block");
		xxx2.doOnNext(System.out::println).next().block();
	}

	@Test
	public void xxx9() {

	}

//	private Mono<Void> recursive(String message) {
//		if ("level1".equals(message)) {
//
//		}
//	}

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

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.S2)
					.join(TestStates.S3)
					.state(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30)
						.state(TestStates.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.S31)
					.event(TestEvents.E3)
					.and()
				.withJoin()
					.source(TestStates.S21)
					.source(TestStates.S31)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.SI)
					.event(TestEvents.E4);
		}

	}
}
