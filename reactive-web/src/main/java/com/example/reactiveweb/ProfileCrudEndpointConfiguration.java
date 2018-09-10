package com.example.reactiveweb;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Configuration
class ProfileCrudEndpointConfiguration {

		private final ProfileService profileService;

		ProfileCrudEndpointConfiguration(ProfileService profileService) {
				this.profileService = profileService;
		}

		@Bean
		RouterFunction<ServerResponse> routes() {
				return route(i(GET("/profiles")), this::all)
					.andRoute(i(GET("/profiles/{id}")), this::getById)
					.andRoute(i(DELETE("/profiles/{id}")), this::deleteById)
					.andRoute(i(POST("/profiles")), this::create)
					.andRoute(i(PUT("/profiles")), this::updateById);
		}

		private Mono<ServerResponse> getById(ServerRequest r) {
				return defaultJsonResponse(this.profileService.byId(id(r)));
		}

		private Mono<ServerResponse> all(ServerRequest r) {
				return defaultJsonResponse(this.profileService.all());
		}

		private Mono<ServerResponse> deleteById(ServerRequest r) {
				return defaultJsonResponse(this.profileService.delete(id(r)));
		}

		private Mono<ServerResponse> updateById(ServerRequest r) {
				Flux<Profile> id = r.bodyToFlux(Profile.class)
					.flatMap(p -> this.profileService.update(id(r), p.getEmail()));
				return defaultJsonResponse(id);
		}

		private Mono<ServerResponse> create(ServerRequest r) {
				Flux<Profile> map = r.bodyToFlux(Profile.class)
					.flatMap(p -> this.profileService.create(p.getEmail()));
				return defaultJsonResponse(map);
		}

		private static Mono<ServerResponse> defaultJsonResponse(Publisher<Profile> profiles) {
				return ServerResponse
					.ok()
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.body(profiles, Profile.class);
		}

		private static String id(ServerRequest r) {
				return r.pathVariable("id");
		}

		private static RequestPredicate i(RequestPredicate target) {
				return new CaseInsensitiveRequestPredicate(target);
		}
}
