package net.officefloor.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Mock Spring Boot configuration.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootApplication
public class MockSpringBootConfiguration {

	@Bean
	public QualifiedBean qualifiedOne() {
		return new QualifiedBean("One");
	}

	@Bean("qualifiedTwo")
	public QualifiedBean createTwo() {
		return new QualifiedBean("Two");
	}

	@Bean
	public OfficeFloorManagedObject officeFloorManagedObject() {
		return SpringSupplierSource.getManagedObject(null, OfficeFloorManagedObject.class);
	}

}