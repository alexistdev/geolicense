package com.alexistdev.geolicense;

import tools.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@SpringBootApplication
public class GeolicenseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeolicenseApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.createTypeMap(Date.class, LocalDateTime.class)
				.setConverter(
						context -> context.getSource() == null ? null
								: LocalDateTime.ofInstant(
								context.getSource().toInstant(),
								ZoneId.systemDefault()));
		return modelMapper;
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
