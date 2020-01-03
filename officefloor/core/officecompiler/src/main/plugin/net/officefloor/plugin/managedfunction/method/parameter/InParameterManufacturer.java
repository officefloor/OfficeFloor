package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.variable.In;

/**
 * {@link MethodParameterManufacturer} for a {@link In}.
 * 
 * @author Daniel Sagenschneider
 */
public class InParameterManufacturer extends AbstractVariableParameterManufacturer {

	@Override
	protected Class<?> getParameterClass() {
		return In.class;
	}

	@Override
	protected MethodParameterFactory createMethodParameterFactory(int objectIndex) {
		return new InParameterFactory(objectIndex);
	}

}