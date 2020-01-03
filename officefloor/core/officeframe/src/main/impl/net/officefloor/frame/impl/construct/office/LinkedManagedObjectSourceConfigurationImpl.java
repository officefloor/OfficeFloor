package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;

/**
 * {@link LinkedManagedObjectSourceConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkedManagedObjectSourceConfigurationImpl implements
		LinkedManagedObjectSourceConfiguration {

	/**
	 * {@link Office} name of the {@link ManagedObject}.
	 */
	private final String officeManagedObjectName;

	/**
	 * {@link OfficeFloor} name of the {@link ManagedObjectSource}.
	 */
	private final String officeFloorManagedObjectSourceName;

	/**
	 * Initiate.
	 * 
	 * @param officeManagedObjectName
	 *            {@link Office} name of the {@link ManagedObject}.
	 * @param officeFloorManagedObjectSourceName
	 *            {@link OfficeFloor} name of the {@link ManagedObjectSource}.
	 */
	public LinkedManagedObjectSourceConfigurationImpl(String officeManagedObjectName,
			String officeFloorManagedObjectSourceName) {
		this.officeManagedObjectName = officeManagedObjectName;
		this.officeFloorManagedObjectSourceName = officeFloorManagedObjectSourceName;
	}

	/*
	 * ================ LinkedManagedObjectSourceConfiguration ================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.officeManagedObjectName;
	}

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.officeFloorManagedObjectSourceName;
	}

}