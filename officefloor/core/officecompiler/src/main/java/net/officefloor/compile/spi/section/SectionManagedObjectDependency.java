package net.officefloor.compile.spi.section;

import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Dependency for a section {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionManagedObjectDependency extends ManagedObjectDependency, SectionDependencyRequireNode {
}