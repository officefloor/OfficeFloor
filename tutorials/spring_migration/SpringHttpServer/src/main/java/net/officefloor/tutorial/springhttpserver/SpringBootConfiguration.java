package net.officefloor.tutorial.springhttpserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import net.officefloor.spring.SpringSupplierSource;

/**
 * Provides Spring configuration.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@SpringBootApplication
public class SpringBootConfiguration {

	@Bean
	public Other other() {
		return SpringSupplierSource.getManagedObject(null, Other.class);
	}
}
// END SNIPPET: tutorial