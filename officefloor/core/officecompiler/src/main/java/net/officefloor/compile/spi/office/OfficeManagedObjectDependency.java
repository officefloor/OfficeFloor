package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link Office} {@link ManagedObjectDependency}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectDependency extends ManagedObjectDependency, OfficeDependencyRequireNode {
}