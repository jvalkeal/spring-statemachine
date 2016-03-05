/*
 * Copyright 2016 the original author or authors.
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
package demo.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.web.context.request.DestructionCallbackBindingListener;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import demo.session.StateMachineConfig.Events;
import demo.session.StateMachineConfig.States;

public class KryoRedisSerializer implements RedisSerializer<Object> {

	private KryoPool pool;
	private BeanFactory beanFactory;

	public KryoRedisSerializer() {
		KryoFactory factory = new KryoFactory() {
			public Kryo create() {
				Kryo kryo = new Kryo();
				kryo.addDefaultSerializer(DestructionCallbackBindingListener.class, new JavaSerializer());
				kryo.addDefaultSerializer(StateMachine.class, new StateMachineSerializer(beanFactory));
				return kryo;
			}
		};
		pool = new KryoPool.Builder(factory).softReferences().build();
	}

	@Override
	public byte[] serialize(Object t) throws SerializationException {
		Kryo kryo = pool.borrow();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeClassAndObject(output, t);
		output.close();
		pool.release(kryo);
		return out.toByteArray();
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Kryo kryo = pool.borrow();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		Input input = new Input(in);
		Object object = kryo.readClassAndObject(input);
		pool.release(kryo);
		return object;
	}

	@Autowired
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public static class StateMachineSerializer extends Serializer<StateMachine<States, Events>> {

		private BeanFactory beanFactory;

		public StateMachineSerializer(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public void write(Kryo kryo, Output output, StateMachine<States, Events> stateMachine) {
			// serialize StateMachineContext which is a representation of a state machine
			output.write(1);
		}

		@Override
		public StateMachine<States, Events> read(Kryo kryo, Input input, Class<StateMachine<States, Events>> clazz) {
			// build and reset a machine from a factory registry.
			int readInt = input.readInt();
			Builder<States, Events> builder = StateMachineBuilder.<States, Events>builder();

			try {
				builder.configureConfiguration()
					.withConfiguration()
						.taskExecutor(new SyncTaskExecutor())
						.autoStartup(true)
						.beanFactory(beanFactory);

				builder.configureStates()
					.withStates()
						.initial(States.S0)
						.states(EnumSet.allOf(States.class));

				builder.configureTransitions()
					.withExternal()
						.source(States.S0).target(States.S1).event(Events.A)
						.and()
					.withExternal()
						.source(States.S1).target(States.S2).event(Events.B)
						.and()
					.withExternal()
						.source(States.S2).target(States.S0).event(Events.C);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return builder.build();
//			return null;
		}

	}


}
