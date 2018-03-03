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
package org.springframework.statemachine.dsl;

import org.springframework.core.io.Resource;

/**
 * Interface for a parser able to parse an a content into a dsl result.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the type of {@link DslParserResult} value
 */
public interface DslParser<T> {

	/**
	 * Parse given {@link Resource} into a {@link DslParserResult}.
	 *
	 * @param resource the parse source
	 * @return the parsed {@code DslParserResult}
	 */
	DslParserResult<T> parse(Resource resource);
}