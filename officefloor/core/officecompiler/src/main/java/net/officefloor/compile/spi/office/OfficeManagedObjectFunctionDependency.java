package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * {@link Office} {@link ManagedObjectFunctionDependency}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectFunctionDependency extends ManagedObjectDependency, OfficeDependencyRequireNode {
}