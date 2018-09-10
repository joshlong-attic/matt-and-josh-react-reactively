package com.example.reactiveweb;

import org.springframework.http.server.PathContainer;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.ServerRequestWrapper;

import java.net.URI;

/**
	*
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
public class CaseInsensitiveRequestPredicate implements RequestPredicate {

		private final RequestPredicate target;

		@Override
		public String toString() {
				return this.target.toString();
		}

		CaseInsensitiveRequestPredicate(RequestPredicate target) {
				this.target = target;
		}

		@Override
		public boolean test(ServerRequest request) {
				return this.target.test(new LowerCaseUriServerRequestWrapper(request));
		}
}

class LowerCaseUriServerRequestWrapper extends ServerRequestWrapper {

		LowerCaseUriServerRequestWrapper(ServerRequest delegate) {
				super(delegate);
		}

		@Override
		public URI uri() {
				return URI.create(super.uri().toString().toLowerCase());
		}

		@Override
		public String path() {
				return uri().getRawPath();
		}

		@Override
		public PathContainer pathContainer() {
				return PathContainer.parsePath(path());
		}
}
