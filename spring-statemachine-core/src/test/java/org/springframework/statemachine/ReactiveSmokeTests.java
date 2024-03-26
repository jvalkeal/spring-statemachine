package org.springframework.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class ReactiveSmokeTests extends AbstractStateMachineTests {

	private final static Log log = LogFactory.getLog(ReactiveSmokeTests.class);

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	// @Test
	public void smoke1() {
		context.register(Config1.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		assertThat(machine).isNotNull();
		doStartAndAssert(machine);
		doSendEventAndConsumeAll(machine, MessageBuilder.withPayload(TestEvents.E1).build());
	}

	@Test
	public void smoke2() throws InterruptedException {
		context.register(Config1.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		assertThat(machine).isNotNull();

		int count = 10;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < count; i++) {
			Thread t = new Thread(() -> {
				doStartAndAssert(machine);
				doSendEventAndConsumeAll(machine, MessageBuilder.withPayload(TestEvents.E1).build());
			}, "t" + i);
			threads.add(t);
		}
		for (Thread thread : threads) {
			thread.run();
		}
		for (Thread thread : threads) {
			// log.info("Join " + thread);
			thread.join();
		}

		// doSendEventAndConsumeAll(machine, MessageBuilder.withPayload(TestEvents.E1).build());
		// doStartAndAssert(machine);
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
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S1)
					.event(TestEvents.E3);
		}
	}

}
