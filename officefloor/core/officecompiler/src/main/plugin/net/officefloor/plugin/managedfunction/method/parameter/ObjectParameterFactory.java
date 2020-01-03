package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link MethodParameterFactory} for an {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectParameterFactory implements MethodParameterFactory {

	/**
	 * Index of the {@link Object}.
	 */
	protected final int objectIndex;

	/**
	 * Initiate.
	 * 
	 * @param objectIndex Index of the {@link Object}.
	 */
	public ObjectParameterFactory(int objectIndex) {
		this.objectIndex = objectIndex;
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return context.getObject(this.objectIndex);
	}

}