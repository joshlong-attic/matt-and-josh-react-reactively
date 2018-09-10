package com.example.reactiveweb;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Log4j2
@SpringBootApplication
public class ReactiveWebApplication {

		@org.springframework.context.annotation.Profile("demo")
		@Bean
		ApplicationRunner run(ProfileRepository ps) {
				return args -> {

						Flux<Object> other = Flux
							.just("A", "B", "C", "D")
							.flatMap(name -> ps.save(new Profile(UUID.randomUUID().toString(), name + "@email.com")));

						ps
							.deleteAll()
							.thenMany(other)
							.thenMany(ps.findAll())
							.subscribe(profile -> log.info("saving " + profile.toString()));

				};
		}

		public static void main(String[] args) {
				SpringApplication.run(ReactiveWebApplication.class, args);
		}
}


