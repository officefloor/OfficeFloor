package net.officefloor.spring.webmvc;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.supply.extension.BeforeCompleteServletSupplierExtensionContext;
import net.officefloor.servlet.supply.extension.ServletSupplierExtension;
import net.officefloor.servlet.supply.extension.ServletSupplierExtensionServiceFactory;
import net.officefloor.spring.SpringSupplierSource;

/**
 * Web MVC {@link ServletSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebMvcServletSupplierExtension
		implements ServletSupplierExtensionServiceFactory, ServletSupplierExtension {

	/*
	 * ================== ServletSupplierExtensionServiceFactory ==================
	 */

	@Override
	public ServletSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================== ServletSupplierExtension ========================
	 */

	@Override
	public void beforeCompletion(BeforeCompleteServletSupplierExtensionContext context) throws Exception {

		// Force start Spring (which in turn should force start the Servlet container)
		SpringSupplierSource.forceStartSpring();
	}

}
