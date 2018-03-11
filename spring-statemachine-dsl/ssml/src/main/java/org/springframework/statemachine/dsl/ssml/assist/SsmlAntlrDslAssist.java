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
package org.springframework.statemachine.dsl.ssml.assist;

import org.springframework.statemachine.dsl.DslAssist;
import org.springframework.statemachine.dsl.antlr.assist.AntlrDslAssist;
import org.springframework.statemachine.dsl.ssml.SsmlAntlrFactory;

/**
 * {@code SSML} related {@link DslAssist} implementation.
 *
 * @author Janne Valkealahti
 *
 */
public class SsmlAntlrDslAssist extends AntlrDslAssist {

    /**
     * Instantiate a new Ssml Antlr Dsl Assist.
     */
	public SsmlAntlrDslAssist() {
		super(new SsmlAntlrFactory());
	}
}
