package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * Creates the value for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueParameterFactory extends ObjectParameterFactory {

	/**
	 * Instantiate.
	 * 
	 * @param objectIndex Object index.
	 */
	public ValueParameterFactory(int objectIndex) {
		super(objectIndex);
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return VariableManagedObjectSource.val(context.getObject(this.objectIndex));
	}

}