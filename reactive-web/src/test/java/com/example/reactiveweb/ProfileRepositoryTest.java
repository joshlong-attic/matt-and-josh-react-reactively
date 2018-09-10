package com.example.reactiveweb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.Predicate;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@DataMongoTest
@RunWith(SpringRunner.class)
public class ProfileRepositoryTest {

		@Autowired
		private ProfileRepository profileRepository;

		@Test
		public void query() throws Exception {

				Profile josh = new Profile(UUID.randomUUID().toString(), "Josh");
				Profile matt = new Profile(UUID.randomUUID().toString(), "Matt");
				Profile jane = new Profile(UUID.randomUUID().toString(), "Jane");

				Flux<Profile> saved = this.profileRepository.saveAll(Flux.just(matt, jane, josh));

				Flux<Profile> composite = this.profileRepository
					.deleteAll()
					.thenMany(saved);

				Predicate<Profile> match = profile -> saved.any(saveItem -> saveItem.equals(profile)).block();

				StepVerifier
					.create(composite)
					.expectNextMatches(match)
					.expectNextMatches(match)
					.expectNextMatches(match)
					.verifyComplete();
		}

}