package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.variable.Var;

/**
 * {@link MethodParameterManufacturer} for a {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableParameterManufacturer extends AbstractVariableParameterManufacturer {

	@Override
	protected Class<?> getParameterClass() {
		return Var.class;
	}

	@Override
	protected MethodParameterFactory createMethodParameterFactory(int objectIndex) {
		return new VariableParameterFactory(objectIndex);
	}

}