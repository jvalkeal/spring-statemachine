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
package org.springframework.statemachine.dsl.ssmlserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dsl.lsp.service.Completioner;
import org.springframework.dsl.lsp.service.Hoverer;
import org.springframework.dsl.reconcile.Linter;

/**
 * Configuration for a {@code simple} sample language supporting
 * {@link Hoverer}, {@link Completioner} and {@link Linter}.
 *
 * @author Janne Valkealahti
 * @see EnableSsmlLanguage
 *
 */
@Configuration
public class SsmlLanguageConfiguration {

	@Bean
	public Hoverer ssmlHoverer() {
		return new SsmlHoverer();
	}

	@Bean
	public Completioner ssmlCompletioner() {
		return new SsmlCompletioner();
	}

	@Bean
	public Linter ssmlLinter() {
//		return new SsmlLinter();
		return null;
	}
}
