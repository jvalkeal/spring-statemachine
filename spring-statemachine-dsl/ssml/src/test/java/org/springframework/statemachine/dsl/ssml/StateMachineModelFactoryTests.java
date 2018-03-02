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
package org.springframework.statemachine.dsl.ssml;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.dsl.DslStateMachineModelFactory;

public class StateMachineModelFactoryTests {

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		context = new AnnotationConfigApplicationContext();
	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleMachineNormal() {
		context.register(Config1.class);
		context.refresh();

		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), contains("S3"));
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource ssml = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-normal.ssml");
			return new DslStateMachineModelFactory(ssml, new SsmlDslParser());
		}
	}
}
