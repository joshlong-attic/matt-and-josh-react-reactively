package com.example.reactiveweb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
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
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Log4j2
@Configuration
class WebSocketConfiguration {

		@Bean
		Executor executor() {
				return Executors.newSingleThreadExecutor();
		}

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
		public static class ProfileCreatedEventPublisher implements
			ApplicationListener<ProfileCreatedEvent>,
			Consumer<FluxSink<ProfileCreatedEvent>> {

				private final Executor executor;
				private final BlockingQueue<ProfileCreatedEvent> queue = new LinkedBlockingQueue<>();

				ProfileCreatedEventPublisher(Executor executor) {
						this.executor = executor;
				}

				@Override
				public void onApplicationEvent(ProfileCreatedEvent event) {
						this.queue.offer(event);
						log.info("queue.offer(" + event + ")");
				}

				@Override
				public void accept(FluxSink<ProfileCreatedEvent> sink) {
						this.executor.execute(() -> {
								while (true)
										try {
												log.info("take()'ing the next result...");
												ProfileCreatedEvent event = queue.take();
												sink.next(event);
												log.info("sink.next(" + event + ")");
										}
										catch (InterruptedException e) {
												ReflectionUtils.rethrowRuntimeException(e);
										}
						});
				}
		}

		@Bean
		WebSocketHandler webSocketHandler(ProfileCreatedEventPublisher profileCreatedEventPublisher) {

				ObjectMapper objectMapper = new ObjectMapper();

				return session -> {
						Flux<ProfileCreatedEvent> publish = Flux.create(profileCreatedEventPublisher) .publish().autoConnect();
						Flux<WebSocketMessage> messageFlux = publish
							.map(evt -> {
									try {
											return objectMapper.writeValueAsString(evt.getSource());
									}
									catch (JsonProcessingException e) {
											throw new RuntimeException(e);
									}
							})
							.map(str -> {
									log.info("sending " + str);
									return session.textMessage(str);
							});

						return session.send(messageFlux);
				};
		}

		@Bean
		WebSocketHandlerAdapter webSocketHandlerAdapter() {
				return new WebSocketHandlerAdapter();
		}
}
