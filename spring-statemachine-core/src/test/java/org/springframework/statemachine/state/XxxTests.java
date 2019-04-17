package org.springframework.statemachine.state;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.AbstractStateMachineTests.BaseConfig;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.state.SubmachineStateTests.Config3;

public class XxxTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testXxx1() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent("E1");

		assertThat(machine.getState().getIds(), contains("S2", "S20", "S2012"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testXxx2() throws Exception {
		context.register(BaseConfig.class, Config12.class, Config11.class, Config10.class);
//		context.register(BaseConfig.class, Config12.class, Config10.class);
		context.refresh();
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent("E1");

		assertThat(machine.getState().getIds(), contains("S2", "S20", "S2012"));
//		assertThat(machine.getState().getIds(), contains("S2", "S2012"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.and()
					.withStates()
						.parent("S2")
						.initial("S20")
						.and()
						.withStates()
							.parent("S20")
							.initial("S2011")
							.choice("CHOICE")
							.state("S2012");
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
					.source("S2011")
					.target("CHOICE")
					.and()
				.withChoice()
					.source("CHOICE")
					.last("S2012");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config10 extends StateMachineConfigurerAdapter<String, String> {

//		@Autowired
//		@Qualifier("subStateMachine1")
		private StateMachine<String, String> subStateMachine;

		public Config10(@Qualifier("subStateMachine1") StateMachine<String, String> subStateMachine) {
			this.subStateMachine = subStateMachine;
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", subStateMachine);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
		}

	}

	@Configuration
	@EnableStateMachine(name = "subStateMachine1")
	static class Config11 extends StateMachineConfigurerAdapter<String, String> {

//		@Autowired
//		@Qualifier("subStateMachine2")
		private StateMachine<String, String> subStateMachine;

		public Config11(@Qualifier("subStateMachine2") StateMachine<String, String> subStateMachine) {
			this.subStateMachine = subStateMachine;
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S20")
					.state("S20", subStateMachine)
					.state("S21");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S20")
					.target("S21")
					.event("E2");
		}
	}

	@Configuration
	@EnableStateMachine(name = "subStateMachine2")
	static class Config12 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S2011")
					.choice("CHOICE")
					.state("S2012");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S2011")
					.target("CHOICE")
					.and()
				.withChoice()
					.source("CHOICE")
					.last("S2012");
		}

	}
}
