package net.officefloor.spring.jaxrs;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import net.officefloor.spring.SpringSupplierSource;

/**
 * Spring application.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootApplication
public class SpringApplication {

	@Bean
	public OfficeFloorDependency officeFloorDependency() {
		return SpringSupplierSource.getManagedObject(null, OfficeFloorDependency.class);
	}
}