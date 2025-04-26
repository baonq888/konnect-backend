package com.konnectnet.core;

import com.konnectnet.core.auth.enums.RoleEnum;
import com.konnectnet.core.auth.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

	@Bean
	CommandLineRunner run(AuthService authService) {
		return args -> {
			authService.saveRole(RoleEnum.USER.name());
			authService.saveRole(RoleEnum.GROUP_ADMIN.name());
			authService.saveRole(RoleEnum.GROUP_MEMBER.name());
		};
	}
}
