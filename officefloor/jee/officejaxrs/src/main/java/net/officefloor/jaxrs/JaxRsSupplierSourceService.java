package net.officefloor.jaxrs;

import net.officefloor.compile.SupplierSourceService;
import net.officefloor.compile.SupplierSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * JAX-RS {@link SupplierSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsSupplierSourceService
		implements SupplierSourceService<JaxRsSupplierSource>, SupplierSourceServiceFactory {

	/*
	 * =================== SupplierSourceServiceFactory ===================
	 */

	@Override
	public SupplierSourceService<?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== SupplierSourceService =======================
	 */

	@Override
	public String getSupplierSourceAlias() {
		return "JAXRS";
	}

	@Override
	public Class<JaxRsSupplierSource> getSupplierSourceClass() {
		return JaxRsSupplierSource.class;
	}

}