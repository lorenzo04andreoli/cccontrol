package com.ConselhoDaComunidade.JudicialControl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JudicialControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(JudicialControlApplication.class, args);
	}

}
