package com.example.reactiveweb;

import org.springframework.context.ApplicationEvent;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
public class ProfileCreatedEvent extends ApplicationEvent {

		public ProfileCreatedEvent(Profile source) {
				super(source);
		}
}
