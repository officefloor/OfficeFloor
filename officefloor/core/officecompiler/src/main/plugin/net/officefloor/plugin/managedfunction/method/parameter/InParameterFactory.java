package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * Creates the {@link In} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class InParameterFactory extends ObjectParameterFactory {

	/**
	 * Instantiate.
	 * 
	 * @param objectIndex Object index.
	 */
	public InParameterFactory(int objectIndex) {
		super(objectIndex);
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return VariableManagedObjectSource.in(context.getObject(this.objectIndex));
	}

}