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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.dsl.DslParserResult;

/**
 * Tests for {@link SsmlDslParser}.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlDslParserTests {

	@Test
	public void testSimpleMachineNormal() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-normal.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getModel(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData().getStateData().size(), is(3));
		assertThat(dslParserResult.getModel().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getModel().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getModel().getTransitionsData().getTransitions().size(), is(2));

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));
	}

	@Test
	public void testSimpleMachineMixedorder() throws Exception {
		Resource ssmlResource = new ClassPathResource("org/springframework/statemachine/dsl/ssml/simplemachine-mixedorder.ssml");

		SsmlDslParser ssmlDslParser = new SsmlDslParser();
		DslParserResult<StateMachineModel<String, String>> dslParserResult = ssmlDslParser.parse(ssmlResource);
		assertThat(dslParserResult, notNullValue());
		assertThat(dslParserResult.getModel(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData().getStateData(), notNullValue());
		assertThat(dslParserResult.getModel().getStatesData().getStateData().size(), is(3));
		assertThat(dslParserResult.getModel().getTransitionsData(), notNullValue());
		assertThat(dslParserResult.getModel().getTransitionsData().getTransitions(), notNullValue());
		assertThat(dslParserResult.getModel().getTransitionsData().getTransitions().size(), is(2));

		assertThat(dslParserResult.getErrors(), notNullValue());
		assertThat(dslParserResult.getErrors().size(), is(0));
		assertThat(dslParserResult.hasErrors(), is(false));
	}
}
