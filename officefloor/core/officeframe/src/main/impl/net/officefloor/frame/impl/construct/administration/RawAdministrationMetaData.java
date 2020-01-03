package net.officefloor.frame.impl.construct.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;

/**
 * Raw meta-data for the bound {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawAdministrationMetaData {

	/**
	 * {@link RawBoundManagedObjectMetaData} instances for the
	 * {@link Administration}.
	 */
	private final RawBoundManagedObjectMetaData[] rawBoundManagedObjectMetaData;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<?, ?, ?> administrationMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param rawBoundManagedObjectMetaData
	 *            {@link RawBoundManagedObjectMetaData} instances for the
	 *            {@link Administration}.
	 * @param administrationMetaData
	 *            {@link AdministrationMetaData}.
	 */
	public RawAdministrationMetaData(RawBoundManagedObjectMetaData[] rawBoundManagedObjectMetaData,
			AdministrationMetaData<?, ?, ?> administrationMetaData) {
		this.rawBoundManagedObjectMetaData = rawBoundManagedObjectMetaData;
		this.administrationMetaData = administrationMetaData;
	}

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaData} of the
	 * {@link ManagedObject} instances involved in the {@link Administration}.
	 * 
	 * @return {@link RawBoundManagedObjectMetaData} of the
	 *         {@link ManagedObject} instances involved in the
	 *         {@link Administration}.
	 */
	public RawBoundManagedObjectMetaData[] getRawBoundManagedObjectMetaData() {
		return this.rawBoundManagedObjectMetaData;
	}

	/**
	 * Obtains the {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationMetaData}.
	 */
	public AdministrationMetaData<?, ?, ?> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

}