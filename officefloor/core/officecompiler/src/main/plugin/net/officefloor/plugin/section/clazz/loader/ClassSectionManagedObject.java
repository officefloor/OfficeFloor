package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.section.SectionManagedObject;

/**
 * {@link SectionManagedObject} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionManagedObject {

	/**
	 * {@link SectionManagedObject}.
	 */
	private final SectionManagedObject managedObject;

	/**
	 * {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<?> managedObjectType;

	/**
	 * Instantiate.
	 * 
	 * @param managedObject     {@link SectionManagedObject}.
	 * @param managedObjectType {@link ManagedObjectType}.
	 */
	public ClassSectionManagedObject(SectionManagedObject managedObject, ManagedObjectType<?> managedObjectType) {
		this.managedObject = managedObject;
		this.managedObjectType = managedObjectType;
	}

	/**
	 * Obtains the {@link SectionManagedObject}.
	 * 
	 * @return {@link SectionManagedObject}.
	 */
	public SectionManagedObject getManagedObject() {
		return managedObject;
	}

	/**
	 * Obtains the {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectType}.
	 */
	public ManagedObjectType<?> getManagedObjectType() {
		return managedObjectType;
	}

}