package com.example.demo;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Log4j2
@Component
class ProfileCreatedEventPublisher implements
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
