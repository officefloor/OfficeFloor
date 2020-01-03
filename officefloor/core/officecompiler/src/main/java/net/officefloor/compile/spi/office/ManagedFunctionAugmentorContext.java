package net.officefloor.compile.spi.office;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Context for the {@link ManagedFunctionAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionAugmentorContext extends SourceIssues {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getManagedFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionType} of the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionType} of the {@link ManagedFunction}.
	 */
	ManagedFunctionType<?, ?> getManagedFunctionType();

	/**
	 * Adds an {@link OfficeAdministration} to be done before attempting this
	 * {@link ManagedFunction}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done before this {@link ManagedFunction}.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done before attempting this
	 *            {@link ManagedFunction}.
	 */
	void addPreAdministration(OfficeAdministration administration);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done after completing this
	 * {@link ManagedFunction}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done after this {@link ManagedFunction} is complete.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done after completing this
	 *            {@link ManagedFunction}.
	 */
	void addPostAdministration(OfficeAdministration administration);

	/**
	 * Obtains the {@link AugmentedFunctionObject} for the {@link ManagedFunction}.
	 * 
	 * @param objectName
	 *            Name of the {@link FunctionObject} on the {@link ManagedFunction}.
	 * @return {@link AugmentedFunctionObject}.
	 */
	AugmentedFunctionObject getFunctionObject(String objectName);

	/**
	 * Links the {@link AugmentedFunctionObject} to the {@link OfficeManagedObject}.
	 * 
	 * @param object
	 *            {@link AugmentedFunctionObject}.
	 * @param managedObject
	 *            {@link OfficeManagedObject}.
	 */
	void link(AugmentedFunctionObject object, OfficeManagedObject managedObject);

}