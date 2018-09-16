package com.example.demo;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
class WebSocketConfigurationTest {

		private final WebSocketClient socketClient = new ReactorNettyWebSocketClient();
		private final WebClient webClient = WebClient.builder().build();
		private final Executor executor = Executors.newCachedThreadPool();

		@Test
		public void testNotificationsOnUpdates() throws Exception {

				int count = 10;

				Flux<Profile> written = Flux
					.<Profile>generate(sink -> sink.next(new Profile(UUID.randomUUID().toString(), UUID.randomUUID().toString() + "@email.com")))
					.take(count)
					.flatMap(this::write);

				AtomicLong counter = new AtomicLong();

				this.executor.execute(() -> socketClient
					.execute(URI.create("ws://localhost:8080/ws/profiles"), session -> session
						.send(Flux.just("test").map(session::textMessage))
						.thenMany(session
							.receive()
							.map(WebSocketMessage::getPayloadAsText))
						.doOnNext(str -> counter.incrementAndGet())
						.then())
					.block()
				);

				Flux
					.from(written)
					.blockLast();

				Assertions.assertThat(counter.get()).isEqualTo(count);
		}

		private Publisher<Profile> write(Profile p) {
				return
					this.webClient
						.post()
						.uri("http://localhost:8080/profiles")
						.body(BodyInserters.fromObject(p))
						.retrieve()
						.bodyToMono(String.class)
						.thenReturn(p);
		}
}