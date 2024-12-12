package com.auger.apartments;

import org.springframework.boot.SpringApplication;

public class TestApartmentsApplication {

	public static void main(String[] args) {
		SpringApplication.from(ApartmentsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
