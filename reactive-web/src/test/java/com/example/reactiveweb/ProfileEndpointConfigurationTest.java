package com.example.reactiveweb;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@WebFluxTest
@Import({ProfileEndpointConfiguration.class,
	ProfileService.class})
@RunWith(SpringRunner.class)
public class ProfileEndpointConfigurationTest {

		@Autowired
		private WebTestClient client;

		@MockBean
		private ProfileRepository repository;

		@Test
		public void getAll() {

				Mockito
					.when(this.repository.findAll())
					.thenReturn(Flux.just(new Profile("1", "A"), new Profile("2", "B")));

				this.client
					.get()
					.uri("/profiles")
					.accept(MediaType.APPLICATION_JSON_UTF8)
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
					.expectBody()
					.jsonPath("$.[0].id").isEqualTo("1")
					.jsonPath("$.[0].email").isEqualTo("A")
					.jsonPath("$.[1].id").isEqualTo("2")
					.jsonPath("$.[1].email").isEqualTo("B");
		}

		@Test
		public void save() {
				Profile data = new Profile("123", UUID.randomUUID().toString() + "@email.com");
				Mockito
					.when(this.repository.save(Mockito.any(Profile.class)))
					.thenReturn(Mono.just(data));
				MediaType jsonUtf8 = MediaType.APPLICATION_JSON_UTF8;
				this
					.client
					.post()
					.uri("/profiles")
					.contentType(jsonUtf8)
					.body(Mono.just(data), Profile.class)
					.exchange()
					.expectStatus().isCreated()
					.expectHeader().contentType(jsonUtf8);
		}

		@Test
		public void delete() {
				Profile data = new Profile("123", UUID.randomUUID().toString() + "@email.com");
				Mockito
					.when(this.repository.findById(data.getId()))
					.thenReturn(Mono.just(data));
				Mockito
					.when(this.repository.deleteById(data.getId()))
					.thenReturn(Mono.empty());
				this
					.client
					.delete()
					.uri("/profiles/" + data.getId())
					.exchange()
					.expectStatus().isOk();
		}

		@Test
		public void update() {
				Profile data = new Profile("123", UUID.randomUUID().toString() + "@email.com");

				Mockito
					.when(this.repository.findById(data.getId()))
					.thenReturn(Mono.just(data));

				Mockito
					.when(this.repository.save(data))
					.thenReturn(Mono.just(data));

				this
					.client
					.put()
					.uri("/profiles/" + data.getId())
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.body(Mono.just(data), Profile.class)
					.exchange()
					.expectStatus().isOk();
		}

		@Test
		public void getById() {

				Profile data = new Profile("1", "A");

				Mockito
					.when(this.repository.findById(data.getId()))
					.thenReturn(Mono.just(data)) ;

				this.client
					.get()
					.uri("/profiles/" + data.getId())
					.accept(MediaType.APPLICATION_JSON_UTF8)
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
					.expectBody()
					.jsonPath("$.id").isEqualTo("1")
					.jsonPath("$.email").isEqualTo("A");
		}

/*




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
		}*/
}