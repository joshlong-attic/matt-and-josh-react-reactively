package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;


@WebFluxTest
@Import({ProfileRestController.class})
@ExtendWith(SpringExtension.class)
class ProfileRestControllerTest {

		private final WebTestClient client;

		ProfileRestControllerTest(@Autowired WebTestClient client) {
				this.client = client;
		}

		@Test
		public void greetAll() throws Exception {
				this.client
					.get()
					.uri("http://localhost:8080/hello")
					.exchange()
					.expectStatus().isOk()
					.expectBody(String.class).isEqualTo("hello, world!");
		}

		@Test
		public void greetMatt() throws Exception {
				this.client
					.get()
					.uri("http://localhost:8080/hello?name=Matt")
					.exchange()
					.expectStatus().isOk()
					.expectBody(String.class).isEqualTo("hello, Matt!");
		}

}