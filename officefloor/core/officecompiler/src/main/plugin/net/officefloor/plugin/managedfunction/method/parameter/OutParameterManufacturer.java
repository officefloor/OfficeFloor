package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.variable.Out;

/**
 * {@link MethodParameterManufacturer} for a {@link Out}.
 * 
 * @author Daniel Sagenschneider
 */
public class OutParameterManufacturer extends AbstractVariableParameterManufacturer {

	@Override
	protected Class<?> getParameterClass() {
		return Out.class;
	}

	@Override
	protected MethodParameterFactory createMethodParameterFactory(int objectIndex) {
		return new OutParameterFactory(objectIndex);
	}

}