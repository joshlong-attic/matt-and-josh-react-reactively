package com.example.demo;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Log4j2
@Component
@org.springframework.context.annotation.Profile("demo")
public class SampleDataRunner implements ApplicationRunner {

		private final ProfileRepository repository;

		public SampleDataRunner(ProfileRepository repository) {
				this.repository = repository;
		}

		@Override
		public void run(ApplicationArguments args) {

				Flux<Object> other = Flux
					.just("A", "B", "C", "D")
					.flatMap(name -> repository.save(new Profile(UUID.randomUUID().toString(), name + "@email.com")));

				repository
					.deleteAll()
					.thenMany(other)
					.thenMany(repository.findAll())
					.subscribe(profile -> log.info("saving " + profile.toString()));
		}
}
