package com.example.reactiveweb;

import org.springframework.context.ApplicationEvent;

public class ProfileCreatedEvent extends ApplicationEvent {

		public ProfileCreatedEvent(Profile source) {
				super(source);
		}
}
