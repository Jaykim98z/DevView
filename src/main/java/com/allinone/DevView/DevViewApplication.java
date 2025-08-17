package com.allinone.DevView;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		excludeName = {
				"org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
				"org.springframework.boot.autoconfigure.flyway.FlywayJpaDependencyConfiguration"
		}
)
public class DevViewApplication {
	public static void main(String[] args) {
		SpringApplication.run(DevViewApplication.class, args);
	}
}
