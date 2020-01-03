package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.officefloor.OfficeFloorType;

/**
 * {@link OfficeFloorType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTypeImpl implements OfficeFloorType {

	/**
	 * {@link OfficeFloorPropertyType} instances.
	 */
	private final OfficeFloorPropertyType[] propertyTypes;

	/**
	 * {@link OfficeFloorManagedObjectSourceType} instances.
	 */
	private final OfficeFloorManagedObjectSourceType[] managedObjectSourceTypes;

	/**
	 * {@link OfficeFloorTeamSourceType} instances.
	 */
	private final OfficeFloorTeamSourceType[] teamSourceTypes;

	/**
	 * Initialise.
	 * 
	 * @param propertyTypes
	 *            {@link OfficeFloorPropertyType} instances.
	 * @param managedObjectSourceTypes
	 *            {@link OfficeFloorManagedObjectSourceType} instances.
	 * @param teamSourceTypes
	 *            {@link OfficeFloorTeamSourceType} instances.
	 */
	public OfficeFloorTypeImpl(OfficeFloorPropertyType[] propertyTypes,
			OfficeFloorManagedObjectSourceType[] managedObjectSourceTypes,
			OfficeFloorTeamSourceType[] teamSourceTypes) {
		this.propertyTypes = propertyTypes;
		this.managedObjectSourceTypes = managedObjectSourceTypes;
		this.teamSourceTypes = teamSourceTypes;
	}

	/*
	 * ====================== OfficeFloorType ============================
	 */

	@Override
	public OfficeFloorPropertyType[] getOfficeFloorPropertyTypes() {
		return this.propertyTypes;
	}

	@Override
	public OfficeFloorManagedObjectSourceType[] getOfficeFloorManagedObjectSourceTypes() {
		return this.managedObjectSourceTypes;
	}

	@Override
	public OfficeFloorTeamSourceType[] getOfficeFloorTeamSourceTypes() {
		return this.teamSourceTypes;
	}

}