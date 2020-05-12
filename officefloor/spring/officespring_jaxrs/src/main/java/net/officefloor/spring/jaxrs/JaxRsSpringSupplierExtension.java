package net.officefloor.spring.jaxrs;

import org.springframework.boot.builder.SpringApplicationBuilder;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * JAX-RS {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsSpringSupplierExtension implements SpringSupplierExtension, SpringSupplierExtensionServiceFactory {

	/*
	 * ================== SpringSupplierExtensionServiceFactory ==================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== SpringSupplierExtension ===========================
	 */

	@Override
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Load Jersey on start up
		builder.properties("spring.jersey.servlet.loadOnStartup=1");
		builder.properties("spring.jersey.type=FILTER");
	}

}