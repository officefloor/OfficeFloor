package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedFunction} available to the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionManagedFunction {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getManagedFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionType} for the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionType} for the {@link ManagedFunction}.
	 */
	ManagedFunctionType<?, ?> getManagedFunctionType();

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link ManagedFunctionFlowType}.
	 * 
	 * @param flowType
	 *            {@link ManagedFunctionFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link ManagedFunctionFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(ManagedFunctionFlowType<?> flowType);

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link ManagedFunctionEscalationType}.
	 * 
	 * @param escalationType
	 *            {@link ManagedFunctionEscalationType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link ManagedFunctionEscalationType}. May be <code>null</code>
	 *         if not handled by the application (as handled by
	 *         {@link ManagedObjectSource} / {@link OfficeFloor}).
	 */
	ExecutionManagedFunction getManagedFunction(ManagedFunctionEscalationType escalationType);

	/**
	 * Obtains the {@link ExecutionManagedObject} for the
	 * {@link ManagedFunctionObjectType}.
	 * 
	 * @param objectType
	 *            {@link ManagedFunctionObjectType}.
	 * @return {@link ExecutionManagedObject} for the
	 *         {@link ManagedFunctionObjectType}.
	 */
	ExecutionManagedObject getManagedObject(ManagedFunctionObjectType<?> objectType);

}