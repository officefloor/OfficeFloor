package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeFloorManagedObjectSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceTypeImpl implements
		OfficeFloorManagedObjectSourceType {

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private final String name;

	/**
	 * {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	private final OfficeFloorManagedObjectSourcePropertyType[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the {@link ManagedObjectSource}.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedObjectSource}.
	 */
	public OfficeFloorManagedObjectSourceTypeImpl(String name,
			OfficeFloorManagedObjectSourcePropertyType[] properties) {
		this.name = name;
		this.properties = properties;
	}

	/*
	 * ================= OfficeFloorManagedObjectSourceType =================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.name;
	}

	@Override
	public OfficeFloorManagedObjectSourcePropertyType[] getOfficeFloorManagedObjectSourcePropertyTypes() {
		return this.properties;
	}

}