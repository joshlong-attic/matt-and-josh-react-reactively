package com.example.demo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
interface ProfileRepository extends ReactiveMongoRepository<Profile, String> {
//		Flux<Profile> findByEmail(String email);
}
