package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * Creates the {@link Var} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableParameterFactory extends ObjectParameterFactory {

	/**
	 * Instantiate.
	 * 
	 * @param objectIndex Object index.
	 */
	public VariableParameterFactory(int objectIndex) {
		super(objectIndex);
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return VariableManagedObjectSource.var(context.getObject(this.objectIndex));
	}

}