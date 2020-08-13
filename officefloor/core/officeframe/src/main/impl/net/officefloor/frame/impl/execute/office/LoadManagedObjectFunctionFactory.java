package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedFunctionFactory} to load the object of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedObjectFunctionFactory
		extends StaticManagedFunction<LoadManagedObjectFunctionFactory.Dependencies, None> {

	/**
	 * Dependency keys for the {@link LoadManagedObjectFunctionFactory}.
	 */
	public static enum Dependencies {
		PARAMETER, MANAGED_OBJECT
	}

	/**
	 * Interface for parameter to receive the loaded object of the
	 * {@link ManagedObject}.
	 */
	@FunctionalInterface
	public static interface LoadManagedObjectParameter {

		/**
		 * Loads the object.
		 * 
		 * @param object Object loaded from {@link ManagedObject}.
		 */
		void load(Object object);
	}

	/*
	 * ====================== ManagedFunction ======================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {

		// Obtain the dependencies
		LoadManagedObjectParameter parameter = (LoadManagedObjectParameter) context.getObject(Dependencies.PARAMETER);
		Object object = context.getObject(Dependencies.MANAGED_OBJECT);

		// Load the managed object
		parameter.load(object);
	}

}