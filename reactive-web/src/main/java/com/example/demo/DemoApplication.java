package com.example.demo;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.UUID;

@SpringBootApplication
public class DemoApplication {


		public static void main(String[] args) {
				SpringApplication.run(DemoApplication.class, args);
		}
}


