package net.officefloor.spring.webmvc;

import org.springframework.boot.builder.SpringApplicationBuilder;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.supply.ServletConfigurationInterest;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Spring Web MVC {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebMvcExtension implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

	/**
	 * Completes the {@link ServletConfigurationInterest}.
	 * 
	 * @throws Exception If fails to complete.
	 */
	public static void completeServletConfigurationInterest() throws Exception {
		interest.get().completeInterest();
		interest.remove();
	}

	/**
	 * {@link ThreadLocal} for the {@link ServletConfigurationInterest}.
	 */
	private static final ThreadLocal<ServletConfigurationInterest> interest = new ThreadLocal<>();

	/*
	 * ================== SpringSupplierExtensionServiceFactory =================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {

		// Register the interest
		ServletConfigurationInterest configurationInterest = ServletSupplierSource.registerInterest();
		interest.set(configurationInterest);

		// Return the extension
		return this;
	}

	/*
	 * ======================= SpringSupplierExtension ==========================
	 */

	@Override
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Configure OfficeFloor embedded Tomcat
		builder.sources(OfficeFloorEmbeddedTomcat.class);
	}

}