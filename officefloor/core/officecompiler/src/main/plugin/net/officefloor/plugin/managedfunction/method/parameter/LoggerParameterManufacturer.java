package net.officefloor.plugin.managedfunction.method.parameter;

import java.util.logging.Logger;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;

/**
 * {@link MethodParameterManufacturer} for the {@link Logger}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerParameterManufacturer
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/*
	 * =========== MethodParameterManufacturerServiceFactory ===============
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== MethodParameterManufacturer =====================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Determine if logger
		if (Logger.class.equals(context.getParameterClass())) {
			// Parameter is logger
			return new LoggerParameterFactory();
		}

		// Not function context
		return null;
	}

}
