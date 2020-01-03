package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of an {@link Office}
 * {@link SectionManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectSourceType {

	/**
	 * Obtains the name of this {@link Office}
	 * {@link SectionManagedObjectSource}.
	 * 
	 * @return Name of this {@link Office} {@link SectionManagedObjectSource}.
	 */
	String getOfficeSectionManagedObjectSourceName();

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionManagedObjectTeamType} instances required by this
	 * {@link Office} {@link SectionManagedObjectSource}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return {@link OfficeSectionManagedObjectTeamType} instances required by this
	 *         {@link Office} {@link SectionManagedObjectSource}.
	 */
	OfficeSectionManagedObjectTeamType[] getOfficeSectionManagedObjectTeamTypes();

}