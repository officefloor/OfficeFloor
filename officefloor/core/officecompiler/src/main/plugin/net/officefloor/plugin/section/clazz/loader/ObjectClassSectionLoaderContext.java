package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObject;

/**
 * Context for the {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectClassSectionLoaderContext extends ClassSectionLoaderContext {

	/**
	 * Obtains the {@link SectionManagedObject}.
	 * 
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObject getSectionManagedObject();

	/**
	 * Obtains the {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Flags the {@link ManagedObjectDependency} linked.
	 * 
	 * @param dependencyIndex Index of the {@link ManagedObjectDependency}.
	 */
	void flagObjectDependencyLinked(int dependencyIndex);

}