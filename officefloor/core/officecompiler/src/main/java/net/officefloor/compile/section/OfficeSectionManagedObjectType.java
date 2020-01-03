package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> for an {@link Office}
 * {@link SectionManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectType extends DependentObjectType {

	/**
	 * Obtains the name of this {@link Office} {@link SectionManagedObject}.
	 * 
	 * @return Name of this {@link Office} {@link SectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

	/**
	 * <p>
	 * Obtains the supported extension interfaces by this {@link Office}
	 * {@link SectionManagedObject}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return Supported extension interfaces by this
	 *         {@link OfficeSectionManagedObject}.
	 */
	Class<?>[] getSupportedExtensionInterfaces();

	/**
	 * Obtains the {@link OfficeSectionManagedObjectSourceType} for this
	 * {@link OfficeSectionManagedObject}.
	 * 
	 * @return {@link OfficeSectionManagedObjectSourceType} for this
	 *         {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObjectSourceType getOfficeSectionManagedObjectSourceType();

}