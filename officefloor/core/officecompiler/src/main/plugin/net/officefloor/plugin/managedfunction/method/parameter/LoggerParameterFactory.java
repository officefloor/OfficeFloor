package net.officefloor.plugin.managedfunction.method.parameter;

import java.util.logging.Logger;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link MethodParameterFactory} for the {@link Logger}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerParameterFactory implements MethodParameterFactory {

	/*
	 * ====================== ParameterFactory =============================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return context.getLogger();
	}

}