package org.springframework.statemachine.support;

import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Processor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.WorkQueueProcessor;
import reactor.test.StepVerifier;

@Ignore
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

	@Test
	public void test10() throws Exception {
		EmitterProcessor<Integer> emitter = EmitterProcessor.create();
		BlockingSink<Integer> sink = emitter.connectSink();
	}


}
