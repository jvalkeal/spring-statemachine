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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.service.ReactiveLockingStateMachineHandlerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import reactor.core.publisher.Mono;

@Controller
public class StateMachineController {

	@Autowired
	private StateMachineFactory<String, String> stateMachineFactory;

	@Autowired
	private TransitionRepository<? extends RepositoryTransition> transitionRepository;

	@Autowired
	private ReactiveLockingStateMachineHandlerService<String, String> service;

	// @RequestMapping("/")
	// public String home() {
	// 	return "redirect:/state";
	// }

	@RequestMapping("/test")
	public Mono<String> test(@RequestParam(value = "event", required = true) String event) {
		Mono<String> mono1 = service.handleReactivelyWhileLocked("machine2", machine -> {
			Mono<Message<String>> e = Mono.just(MessageBuilder.withPayload(event).build());
			return machine.sendEvent(e)
				.then(Mono.fromCallable(() -> machine.getState().getId()));
		});
		return mono1;
	}

	// @RequestMapping("/state")
	// public String feedAndGetStates(@RequestParam(value = "events", required = false) List<String> events, Model model) throws Exception {

	// 	Mono<String> mono1 = service.handleReactivelyWhileLocked("machine2", machine -> {
	// 		Mono<Message<String>> event = Mono.just(MessageBuilder.withPayload("E1").build());
	// 		return machine.sendEvent(event)
	// 			.then(Mono.fromCallable(() -> machine.getState().getId()));
	// 	});

	// 	IReactiveDataDriverContextVariable reactiveDataDrivenMode =
    //             new ReactiveDataDriverContextVariable(mono1, 1);


	// 	List<String> xxx = Arrays.asList("hi");
	// 	model.addAttribute("allEvents", getEvents());
	// 	model.addAttribute("messages", createMessages(xxx));
	// 	return "states";
	// }

	private String[] getEvents() {
		List<String> events = new ArrayList<>();
		for (RepositoryTransition t : transitionRepository.findAll()) {
			events.add(t.getEvent());
		}
		return events.toArray(new String[0]);
	}

	private String createMessages(List<String> messages) {
		StringBuilder buf = new StringBuilder();
		for (String message : messages) {
			buf.append(message);
			buf.append("\n");
		}
		return buf.toString();
	}

}
