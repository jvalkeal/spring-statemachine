package org.springframework.statemachine.support;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Processor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.reactive.EventResult;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.ObjectUtils;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.WorkQueueProcessor;
import reactor.test.StepVerifier;

//@Ignore
public class ReactorConceptTests {

	@Test
	public void test1() {
		Flux<String> flux = emptyFlux();

		StepVerifier.create(flux)
				.expectComplete()
				.verify();
	}

	Flux<String> emptyFlux() {
		return Flux.empty();
	}

	@Test
	public void test2() {
		WorkQueueProcessor<Integer> queue = WorkQueueProcessor.create();
		queue.subscribe(System.out::println);
		queue.subscribe(System.out::println);
		queue.onNext(1); //output : ...1
		queue.onNext(2); //output : .... ...2
		queue.onNext(3); //output : ...3
		queue.onComplete();
	}

//	Flux.from(eventProcessor).subscribe(System.out::println);

	@Test
	public void test3() throws Exception {
		Processor<Message<String>, Message<String>> eventProcessor = WorkQueueProcessor.create();
		Processor<Message<String>, Message<String>> deferProcessor = WorkQueueProcessor.create();
		Processor<Message<String>, Message<String>> triggerProcessor = WorkQueueProcessor.create();

		Flux.from(eventProcessor)
			.doOnNext(m -> {
				String payload = m.getPayload();
				if (ObjectUtils.nullSafeEquals(payload, "2")) {
					deferProcessor.onNext(m);
				}
			}).log().subscribe();


		Flux<Message<String>> eventFlux = Flux.merge(eventProcessor, deferProcessor);
		Flux.from(eventFlux).subscribe(triggerProcessor);
		Flux.from(triggerProcessor).doOnNext(System.out::println).subscribe();

		eventProcessor.onNext(MessageBuilder.withPayload("1").build());
		eventProcessor.onNext(MessageBuilder.withPayload("2").build());
		deferProcessor.onNext(MessageBuilder.withPayload("3").build());
		Thread.sleep(1000);
	}

	@Test
	public void test4() throws Exception {
		Processor<Message<String>, Message<String>> deferProcessor = WorkQueueProcessor.create();

		Flux.from(deferProcessor).take(1).doOnNext(System.out::println).subscribe();
		deferProcessor.onNext(MessageBuilder.withPayload("1").build());
		deferProcessor.onNext(MessageBuilder.withPayload("2").build());
		Flux.from(deferProcessor).take(1).doOnNext(System.out::println).subscribe();
		Thread.sleep(1000);
	}

	@Test
	public void test5() throws Exception {
		Processor<Message<String>, Message<String>> eventProcessor = WorkQueueProcessor.create();
		Processor<Message<String>, Message<String>> deferProcessor = WorkQueueProcessor.create();
		Processor<Message<String>, Message<String>> triggerProcessor = WorkQueueProcessor.create();

		Flux.from(eventProcessor)
			.doOnNext(m -> {
				String payload = m.getPayload();
				if (ObjectUtils.nullSafeEquals(payload, "2")) {
					deferProcessor.onNext(m);
				}
			}).log().subscribe();


//		Flux<Message<String>> eventFlux = Flux.merge(eventProcessor, deferProcessor);
//		Flux.from(eventFlux).subscribe(triggerProcessor);
//		Flux.from(triggerProcessor).doOnNext(System.out::println).subscribe();

		eventProcessor.onNext(MessageBuilder.withPayload("1").build());
		eventProcessor.onNext(MessageBuilder.withPayload("2").build());
//		deferProcessor.onNext(MessageBuilder.withPayload("3").build());
		Thread.sleep(1000);
	}

	@Test
	public void test9() throws Exception {
		Flux<String> deferredEventFlux = Flux.fromArray(new String[]{"1", "2"});
		deferredEventFlux.doOnNext(System.out::println).subscribe();
		Thread.sleep(1000);
		deferredEventFlux.doOnNext(System.out::println).subscribe();
		Thread.sleep(1000);
	}

	@Test
	public void test6() throws Exception {
		SomeType instance = new SomeType();
		Flux<String> value = instance.getValue1();
		instance.setValue("Some Value");
		value.subscribe(System.out::println);
	}

	@Test
	public void test7() throws Exception {
		SomeType instance = new SomeType();
		Flux<String> value = instance.getValue2();
		instance.setValue("Some Value");
		value.subscribe(System.out::println);
	}

	@Test
	public void test8() throws Exception {
		SomeType instance = new SomeType();
		Flux<String> value = instance.getValue3();
		instance.setValue("Some Value1");
		value.subscribe(System.out::println);
	}

	@Test
	public void test11() throws Exception {
		Flux<Integer> just = Flux.just(1);
		just.doOnNext(System.out::println).subscribe();
	}

	@Test
	public void test12() throws Exception {
		TestExecutor<String, String> executor = new TestExecutor<>();
		executor.onInit();
		Mono<EventResult> mono = executor.sendEvent(MessageBuilder.withPayload("hello").build());
		System.out.println("block1 ");
		EventResult result = mono.block();
		System.out.println("block2 ");
		Thread.sleep(2000);
		assertThat(result, notNullValue());
	}
	
	public static class TestExecutor<S, E> {
		private EmitterProcessor<TriggerQueueItem<S, E>> triggerProcessor = EmitterProcessor.create();
		private EmitterProcessor<Flux<Message<E>>> eventProcessor = EmitterProcessor.create();
		private EmitterProcessor<Mono<Message<E>>> eventProcessor2 = EmitterProcessor.create();

		public void onInit() throws Exception {
			Flux.from(eventProcessor).doOnNext(flux -> {
				flux.doOnNext(message -> {
					System.out.println("doOnNext " + message);
					handleEvent(message);
				}).subscribe();
			}).subscribe();

			Flux.from(triggerProcessor).doOnNext(item -> {
				handleTrigger(item);
			}).subscribe();
		}

		public Mono<EventResult> sendEvent(Message<E> event) {
			System.out.println("sendEvent " + event);
			
			Mono<EventResult> defer = Mono.defer(() -> {
				Flux<Message<E>> messageFlux = Flux.just(event);
				eventProcessor.onNext(messageFlux);
				return Mono.just(new EventResult());
			});
			return defer;
//			Mono<String> xx = null;
//			xx = Mono.error(null);
//			return Mono.just(new EventResult());
		}

		private void handleTrigger(TriggerQueueItem<S, E> item) {
		}

		public void queueTrigger(Trigger<S, E> trigger, Message<E> message) {
			triggerProcessor.onNext(new TriggerQueueItem<S, E>(trigger, message));
		}

		private void handleEvent(Message<E> message) {
			System.out.println("handleEvent " + message);
		}
	}

	private static class TriggerQueueItem<S, E> {
		Trigger<S, E> trigger;
		Message<E> message;
		public TriggerQueueItem(Trigger<S, E> trigger, Message<E> message) {
			this.trigger = trigger;
			this.message = message;
		}
	}
	
	public static class SomeType {
		private String value;

		public void setValue(String value) {
			this.value = value;
		}

		public Flux<String> getValue1() {
			return Flux.just(value);
		}

		public Flux<String> getValue2() {
			return Flux.create(subscriber -> {
				subscriber.next(value);
				subscriber.complete();
			});
		}

		public Flux<String> getValue3() {
			return Flux.defer(() -> Flux.just(value));
		}
	}

//	@Test
//	public void test10() throws Exception {
//		EmitterProcessor<Integer> emitter = EmitterProcessor.create();
//		BlockingSink<Integer> sink = emitter.connectSink();
//	}


}
