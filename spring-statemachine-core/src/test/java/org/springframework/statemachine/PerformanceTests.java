package org.springframework.statemachine;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class PerformanceTests extends AbstractStateMachineTests {

	@Test
	public void testEventPerformance() {
		context.register(Config1.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine =	context.getBean(StateMachine.class);
		machine.start();
		assertThat(machine.getState().getIds(), contains("S1"));
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), contains("S2"));
		machine.sendEvent("E2");
		assertThat(machine.getState().getIds(), contains("S1"));

		long now = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			machine.sendEvent("E1");
			machine.sendEvent("E2");
		}
		System.out.println("Time: " + (System.currentTimeMillis() - now));
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
				.withExternal()
					.source("S2")
					.target("S1")
					.event("E2");
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
