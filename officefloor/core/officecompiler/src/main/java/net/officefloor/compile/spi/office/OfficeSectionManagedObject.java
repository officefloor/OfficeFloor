package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} contained within an {@link OfficeSubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObject extends DependentManagedObject,
		AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObject}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting load
	 * this {@link ManagedObject}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is
	 * the order they will be done.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done before attempting load
	 *            this {@link ManagedObject}.
	 */
	void addPreLoadAdministration(OfficeAdministration administration);

}