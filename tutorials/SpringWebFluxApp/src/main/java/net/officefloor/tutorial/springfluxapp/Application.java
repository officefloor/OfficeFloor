package net.officefloor.tutorial.springfluxapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring application.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = InjectDependency.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}