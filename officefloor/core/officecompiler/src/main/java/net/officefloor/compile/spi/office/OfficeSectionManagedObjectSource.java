package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSource} contained within a {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectSource {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObjectSource}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObjectSource}.
	 */
	String getOfficeSectionManagedObjectSourceName();

	/**
	 * Obtains the {@link OfficeSectionManagedObjectTeam} required by this
	 * {@link OfficeSectionManagedObjectSource}.
	 * 
	 * @param teamName Name of the {@link ManagedObjectTeam}.
	 * @return {@link OfficeSectionManagedObjectTeam}.
	 */
	OfficeSectionManagedObjectTeam getOfficeSectionManagedObjectTeam(String teamName);

	/**
	 * Obtains the {@link OfficeSectionManagedObject} use of this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName Name of the {@link OfficeSectionManagedObject} to
	 *                          obtain.
	 * @return {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName);

}