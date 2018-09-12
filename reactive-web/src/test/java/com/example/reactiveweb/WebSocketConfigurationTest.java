package com.example.reactiveweb;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
class WebSocketConfigurationTest {

		private final WebTestClient webTestClient;

		private final WebSocketClient socketClient = new ReactorNettyWebSocketClient();

		private final WebClient webClient = WebClient.builder().build();

		WebSocketConfigurationTest(@Autowired WebTestClient client) {
				this.webTestClient = client;
		}

		Publisher<Profile> write(Profile p) {
				RestTemplate restTemplate = new RestTemplate();
				URI uri = URI.create("http://localhost:8080/profiles");
				// i need to move this back to reactive WebClient, but just to keep things simple for now..
				restTemplate.exchange(uri, HttpMethod.POST, RequestEntity.post(uri).body(p), String.class);
				return Mono.just(p);
		}

//		@Test
		public void testNotificationsOnUpdates() throws Exception {

				Flux<Profile> written = Flux
					.<Profile>generate(sink -> sink.next(new Profile("1", "2")))
					.take(10)
					.flatMap(this::write)
					.publish();

				Mono<Void> subscriptions = this.socketClient
					.execute(URI.create("ws://localhost:8080/ws/profiles"), session -> {
							return session
								.send(Flux.just("test").map(session::textMessage))
								.thenMany(session
									.receive()
									.map(WebSocketMessage::getPayloadAsText))
								.doOnNext(str -> log.info("websocket body: " + str))
								.then();
					});

				Flux
					.from(subscriptions)
					.publish(x -> Flux.just("A", "B"))

//					.thenMany(Flux.just("A", "B"))
					.subscribe(x -> System.out.println("listening to websockets.."));

				Thread.sleep(1000 * 10);
		}


}