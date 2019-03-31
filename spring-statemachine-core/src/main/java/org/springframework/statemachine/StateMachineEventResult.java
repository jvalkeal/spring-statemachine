/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine;

import org.springframework.messaging.Message;

/**
 * Interface defining a result for sending an event to a statemachine.
 *
 * @author Janne Valkealahti
 *
 * @param <E> the type of event
 */
public interface StateMachineEventResult<E> {

	Message<E> getMessage();
	ResultType getResultType();

	public enum ResultType {
		ACCEPTED,
		DENIED,
		DEFERRED
	}

	public static <E> StateMachineEventResult<E> of(ResultType resultType) {
		return new DefaultStateMachineEventResult<>(null, resultType);
	}

	static class DefaultStateMachineEventResult<E> implements StateMachineEventResult<E> {

		private Message<E> message;
		private ResultType resultType;

		DefaultStateMachineEventResult(Message<E> message, ResultType resultType) {
			this.message = message;
			this.resultType = resultType;
		}

		@Override
		public Message<E> getMessage() {
			return message;
		}

		@Override
		public ResultType getResultType() {
			return resultType;
		}
	}
}
