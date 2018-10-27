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
package org.springframework.statemachine.buildtests.tck.ssml;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.buildtests.tck.AbstractTckTests;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.dsl.DslStateMachineModelFactory;
import org.springframework.statemachine.dsl.ssml.support.SsmlDslParser;

/**
 * Tck tests for machine configs imported from ssml definitions.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlTckTests extends AbstractTckTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleMachine() throws Exception {
		context.register(SimpleMachineConfig.class);
		context.refresh();
		return getStateMachineFromContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(SimpleSubMachineConfig.class);
		context.refresh();
		return getStateMachineFromContext();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(ShowcaseMachineBeansConfig.class, ShowcaseMachineConfig.class);
		context.refresh();
		return getStateMachineFromContext();
	}

	@Configuration
	@EnableStateMachine
	public static class SimpleMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource ssml = new ClassPathResource("org/springframework/statemachine/buildtests/tck/ssml/SimpleMachine.ssml");
			return new DslStateMachineModelFactory(ssml, new SsmlDslParser());
		}
	}

	@Configuration
	@EnableStateMachine
	public static class SimpleSubMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource ssml = new ClassPathResource("org/springframework/statemachine/buildtests/tck/ssml/SimpleSubMachineConfig.ssml");
			return new DslStateMachineModelFactory(ssml, new SsmlDslParser());
		}
	}

	@Configuration
	@EnableStateMachine
	public static class ShowcaseMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource ssml = new ClassPathResource("org/springframework/statemachine/buildtests/tck/ssml/ShowcaseMachineConfig.ssml");
			return new DslStateMachineModelFactory(ssml, new SsmlDslParser());
		}
	}
}
