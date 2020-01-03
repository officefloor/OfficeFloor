package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * {@link OfficeFloor} {@link ManagedObjectFunctionDependency}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObjectFunctionDependency
		extends ManagedObjectDependency, OfficeFloorDependencyRequireNode {
}