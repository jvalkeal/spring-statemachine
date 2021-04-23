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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.util.StringUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class LockingReactiveTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	public void testInitialStates() throws Exception {
		build(null, null, null)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine1</span>");
				assertThat(body).contains("<span>S1</span>");
			});
		build("machine2", null, null)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine2</span>");
				assertThat(body).contains("<span>S1</span>");
			});
		build("machine3", null, null)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine3</span>");
				assertThat(body).contains("<span>S1</span>");
			});
	}

	@Test
	public void testSimpleEvents() throws Exception {
		build("machine1", "E1", null)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine1</span>");
				assertThat(body).contains("<span>S2</span>");
			});
		build("machine2", "E1", null)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine2</span>");
				assertThat(body).contains("<span>S2</span>");
			});
		build("machine3", "E1", null)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine3</span>");
				assertThat(body).contains("<span>S2</span>");
			});
	}


	@Test
	public void testMultipleEvents() throws Exception {
		build("machine1", "E1,E2", 2000l)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine1</span>");
				assertThat(body).contains("<span>S3</span>");
			});
	}

	@Test
	public void testSleeps() throws Exception {
		build("machine1", "E1", 2000l)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine1</span>");
				assertThat(body).contains("<span>S2</span>");
			});
		build("machine1", "E2", 2000l)
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("<span>machine1</span>");
				assertThat(body).contains("<span>S3</span>");
			});
	}

	private ResponseSpec build(String machine, String events, Long sleep) {
		return webClient.get()
			.uri(builder -> {
				builder.path("/state");
				if (StringUtils.hasText(machine)) {
					builder.queryParam("machine", machine);
				}
				if (StringUtils.hasText(events)) {
					builder.queryParam("events", events);
				}
				if (sleep != null) {
					builder.queryParam("sleep", sleep);
				}
				return builder.build();
			})
			.exchange();
	}

}
