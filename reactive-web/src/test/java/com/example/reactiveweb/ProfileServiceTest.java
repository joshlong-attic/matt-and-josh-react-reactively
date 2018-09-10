package com.example.reactiveweb;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.mapping.TextScore;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.Predicate;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Log4j2
@DataMongoTest
@Import(ProfileService.class)
@RunWith(SpringRunner.class)
public class ProfileServiceTest {

		@Autowired
		private ProfileService service;

		@Autowired
		private ProfileRepository repository;

		@Test
		public void save() {
				Mono<Profile> profileMono = this.service.create("email@email.com");
				StepVerifier
					.create(profileMono)
					.expectNextMatches(saved -> StringUtils.hasText(saved.getId()))
					.verifyComplete();
		}

		@Test
		public void delete() {
				String test = "test";
				Mono<Profile> deleted = this.service
					.create(test)
					.flatMap(saved -> this.service.delete(saved.getId()));
				StepVerifier
					.create(deleted)
					.expectNextMatches(profile -> profile.getEmail().equalsIgnoreCase(test))
					.verifyComplete();
		}

		@Test
		public void getAll() {
				Flux<Profile> saved = repository.saveAll(Flux.just(new Profile(null, "Josh"), new Profile(null, "Matt"), new Profile(null, "Jane")));
				Flux<Profile> composite = service.all().thenMany(saved);
				Predicate<Profile> match = profile -> saved.any(saveItem -> saveItem.equals(profile)).block();
				StepVerifier
					.create(composite)
					.expectNextMatches(match)
					.expectNextMatches(match)
					.expectNextMatches(match)
					.verifyComplete();
		}

		@Test
		public void update() throws Exception {
				Mono<Profile> saved = this.service
					.create("test")
					.flatMap(p -> this.service.update(p.getId(), "test1"));
				StepVerifier
					.create(saved)
					.expectNextMatches(p -> p.getEmail().equalsIgnoreCase("test1"))
					.verifyComplete();
		}

		@Test
		public void getById() {
				String test = UUID.randomUUID().toString();
				Mono<Profile> deleted = this.service
					.create(test)
					.flatMap(saved -> this.service.get(saved.getId()));
				StepVerifier
					.create(deleted)
					.expectNextMatches(profile -> StringUtils.hasText(profile.getId()) && test.equalsIgnoreCase(profile.getEmail()))
					.verifyComplete();
		}
}