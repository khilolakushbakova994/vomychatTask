package com.emailSender.vomychatTask;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableAsync
@SpringBootApplication
public class VomyChatTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(VomyChatTaskApplication.class, args);
	}

}
