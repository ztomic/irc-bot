package com.ztomic.ircbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class IrcBotApplication {
	
	private static final Logger log = LoggerFactory.getLogger(IrcBotApplication.class);

	public static synchronized void close(boolean restart) {
		log.info("Shutting down initiated..");
		log.info("Shutting down.. Restart: {}", restart);
		System.exit(restart ? 0 : 33);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(IrcBotApplication.class, args);
	}
	
}
