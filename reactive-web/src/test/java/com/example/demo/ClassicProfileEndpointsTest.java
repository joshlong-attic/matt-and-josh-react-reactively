package com.example.demo;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@Log4j2
@WebFluxTest
@Import({ProfileRestController.class, ProfileService.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("classic")
public class ClassicProfileEndpointsTest extends ProfileEndpointsBaseClass {

		@BeforeAll
		static void before() {
				log.info("running non-classic tests");
		}

		ClassicProfileEndpointsTest(@Autowired WebTestClient client) {
				super(client);
		}
}