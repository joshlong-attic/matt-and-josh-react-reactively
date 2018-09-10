package com.example.reactiveweb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Configuration
class WebSocketConfiguration {

		@Bean
		HandlerMapping handlerMapping() {
				return new SimpleUrlHandlerMapping() {
						{
								setUrlMap(Collections.singletonMap("/ws/pp", webSocketHandler()));
								setOrder(10);
						}
				};
		}

		@Bean
		WebSocketHandler webSocketHandler() {
				return session -> {
						Flux<WebSocketMessage> messageFlux = Flux
							.<String>generate(sink -> sink.next("hello world @ " + Instant.now().toString()))
							.delayElements(Duration.ofSeconds(10))
							.map(session::textMessage);
						return session.send(messageFlux);
				};
		}

		@Bean
		WebSocketHandlerAdapter webSocketHandlerAdapter() {
				return new WebSocketHandlerAdapter();
		}
}
