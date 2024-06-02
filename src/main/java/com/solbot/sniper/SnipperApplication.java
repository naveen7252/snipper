package com.solbot.sniper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SnipperApplication implements CommandLineRunner{

	private static final Logger LOG = LoggerFactory
			.getLogger(SnipperApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SnipperApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Starting sniper...");
	}
}
