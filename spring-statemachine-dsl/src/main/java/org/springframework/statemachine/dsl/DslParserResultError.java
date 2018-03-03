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

/**
 * {@code DslParserResultError} represents errors, warnings and other
 * ambiguities from a parsing operation.
 *
 * @author Janne Valkealahti
 *
 */
public interface DslParserResultError {

	/**
	 * Gets the message related to an error.
	 *
	 * @return the message related to an error
	 */
	String getMessage();

	/**
	 * Gets the line in dsl for this error.
	 *
	 * @return the line in dsl for this error
	 */
	int getLine();

	/**
	 * Gets the position in line in dsl for this error.
	 *
	 * @return the position in line in dsl for this error
	 */
	int getPosition();
}