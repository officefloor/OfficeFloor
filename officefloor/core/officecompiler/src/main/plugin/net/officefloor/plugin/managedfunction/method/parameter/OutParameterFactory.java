package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * Creates the {@link Out} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class OutParameterFactory extends ObjectParameterFactory {

	/**
	 * Instantiate.
	 * 
	 * @param objectIndex Object index.
	 */
	public OutParameterFactory(int objectIndex) {
		super(objectIndex);
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return VariableManagedObjectSource.out(context.getObject(this.objectIndex));
	}

}