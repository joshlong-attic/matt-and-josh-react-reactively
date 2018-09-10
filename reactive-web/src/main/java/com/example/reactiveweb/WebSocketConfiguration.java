package com.example.reactiveweb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Configuration
class WebSocketConfiguration {

		@Bean
		HandlerMapping handlerMapping(WebSocketHandler wsh) {
				return new SimpleUrlHandlerMapping() {
						{
								setUrlMap(Collections.singletonMap("/ws/pp", wsh));
								setOrder(10);
						}
				};
		}

		@Component
		public static class EventConsumer
			implements Consumer<SynchronousSink<ProfileCreatedEvent>>, ApplicationListener<ProfileCreatedEvent> {

				private final BlockingQueue<ProfileCreatedEvent> queue = new LinkedBlockingQueue<>();

				@Override
				public void accept(SynchronousSink<ProfileCreatedEvent> sink) {
						try {
								sink.next(this.queue.take());
						}
						catch (InterruptedException e) {
								ReflectionUtils.rethrowRuntimeException(e);
						}
				}

				@Override
				public void onApplicationEvent(ProfileCreatedEvent event) {
						this.queue.offer(event);
				}
		}

		@Bean
		WebSocketHandler webSocketHandler(EventConsumer eventConsumer) {
				ObjectMapper objectMapper = new ObjectMapper();
				return session -> {
						Flux<WebSocketMessage> messageFlux = Flux
							.generate(eventConsumer)
							.delayElements(Duration.ofSeconds(10))
							.map(evt -> {
									try {
											return objectMapper.writeValueAsString(evt.getSource());
									}
									catch (JsonProcessingException e) {
											throw new RuntimeException(e);
									}
							})
							.map(session::textMessage);

						return session.send(messageFlux);
				};
		}

		@Bean
		WebSocketHandlerAdapter webSocketHandlerAdapter() {
				return new WebSocketHandlerAdapter();
		}
}
