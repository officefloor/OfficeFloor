package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Provides context for the {@link CompileOfficeFloorExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeFloorContext {

	/**
	 * Obtains the {@link OfficeFloorDeployer}.
	 * 
	 * @return {@link OfficeFloorDeployer}.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link DeployedOffice}.
	 * 
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice getDeployedOffice();

	/**
	 * Obtains the {@link OfficeFloorSourceContext}.
	 * 
	 * @return {@link OfficeFloorSourceContext}.
	 */
	OfficeFloorSourceContext getOfficeFloorSourceContext();

	/**
	 * Adds an {@link OfficeFloorManagedObject} for
	 * {@link ClassManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param managedObjectClass
	 *            {@link Class} for the {@link ClassManagedObjectSource}.
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @return {@link OfficeFloorManagedObject}.
	 */
	OfficeFloorManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
			ManagedObjectScope scope);

}