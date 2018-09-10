package com.example.reactiveweb;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.RequestPredicate;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
public class CaseInsensitiveRequestPredicateTest {

		@Test
		public void normalConfigurationFails() throws Exception {
				MockServerRequest request = MockServerRequest.builder().uri(URI.create("/CATS")).build();
				RequestPredicate predicate = GET("/cats");
				Assert.assertFalse("normal request don't match when the request URI or RequestPredicate are not of the same case", predicate.test(request));
		}

		@Test
		public void ciLowercasePredicateUppercaseRequest() {
				RequestPredicate predicate = new CaseInsensitiveRequestPredicate(GET("/cats"));
				MockServerRequest request = MockServerRequest.builder().uri(URI.create("/CATS")).build();
				Assert.assertTrue("normal requests with uppercase request and lowercase predicates do match", predicate.test(request));
		}

}