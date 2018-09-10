package com.example.reactiveweb;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Service
class ProfileService {

		private final ProfileRepository profileRepository;

		ProfileService(ProfileRepository profileRepository) {
				this.profileRepository = profileRepository;
		}

		public Flux<Profile> all() {
				return this.profileRepository.findAll();
		}

		public Mono<Profile> get(String id) {
				return this.profileRepository.findById(id);
		}

		public Mono<Profile> create(String email) {
				return this.profileRepository.save(new Profile(null, email));
		}

		public Mono<Profile> update(String id, String email) {
				return this.profileRepository
					.findById(id)
					.map(p -> new Profile(p.getId(), email))
					.flatMap(this.profileRepository::save);
		}

		public Mono<Profile> delete(String id) {
				return this.profileRepository
					.findById(id)
					.flatMap(p -> this.profileRepository.deleteById(p.getId()).thenReturn(p));
		}

}
