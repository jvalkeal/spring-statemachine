/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.lockingreactive;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.service.ReactiveLockingStateMachineHandlerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class StateMachineController {

	private final static List<String> EVENTS = Arrays.asList("E1", "E2", "E3");
	private final static List<String> MACHINES = Arrays.asList("machine1", "machine2", "machine3");

	@Autowired
	private ReactiveLockingStateMachineHandlerService<String, String> service;

	@RequestMapping("/")
	public String home() {
		return "redirect:/state";
	}

	@RequestMapping("/state")
	public String feedAndGetStates(
			@RequestParam(value = "machine", required = false, defaultValue = "machine1") String machine,
			@RequestParam(value = "events", required = false) List<String> events,
			Model model) throws Exception {
		model.addAttribute("events", EVENTS);
		model.addAttribute("machines", MACHINES);
		model.addAttribute("machine", machine);

		// build messages or empty if not given
		Flux<Message<String>> messages = !ObjectUtils.isEmpty(events)
				? Flux.fromIterable(events).map(e -> MessageBuilder.withPayload(e).build())
				: Flux.empty();

		// handle while locked and return machine state id after
		Mono<String> state = service.handleReactivelyWhileLocked(machine, stateMachine -> {
			return stateMachine.sendEvents(messages)
				.then(Mono.fromCallable(() -> stateMachine.getState().getId()));
		});

		// pass state mono to thymeleaf which resolves it reactively
		// convert to flux as thymeleaf cannot handle mono
		IReactiveDataDriverContextVariable variable = new ReactiveDataDriverContextVariable(state.flux(), 1);
		model.addAttribute("state", variable);
		return "states";
	}
}
