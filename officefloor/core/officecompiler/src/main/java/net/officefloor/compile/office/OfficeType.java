package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeType {

	/**
	 * Obtains the {@link OfficeInput} <code>type definitions</code> required by
	 * this {@link OfficeType}.
	 * 
	 * @return {@link OfficeInput} <code>type definitions</code> required by
	 *         this {@link OfficeType}.
	 */
	OfficeInputType[] getOfficeInputTypes();

	/**
	 * Obtains the {@link OfficeOutput} <code>type definitions</code> required
	 * by this {@link OfficeType}.
	 * 
	 * @return {@link OfficeOutput} <code>type definitions</code> required by
	 *         this {@link OfficeType}.
	 */
	OfficeOutputType[] getOfficeOutputTypes();

	/**
	 * Obtains the {@link Team} <code>type definitions</code> required by this
	 * {@link OfficeType}.
	 * 
	 * @return {@link Team} <code>type definitions</code> required by this
	 *         {@link OfficeType}.
	 */
	OfficeTeamType[] getOfficeTeamTypes();

	/**
	 * Obtains the {@link ManagedObject} <code>type definition</code> required
	 * by this {@link OfficeType}.
	 * 
	 * @return {@link ManagedObject} <code>type definition</code> required by
	 *         this {@link OfficeType}.
	 */
	OfficeManagedObjectType[] getOfficeManagedObjectTypes();

	/**
	 * Obtains the {@link OfficeSectionInput} <code>type definition</code>
	 * available for this {@link OfficeType}.
	 * 
	 * @return {@link OfficeSectionInput} <code>type definition</code> available
	 *         for this {@link OfficeType}.
	 */
	OfficeAvailableSectionInputType[] getOfficeSectionInputTypes();

}