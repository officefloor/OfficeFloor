package net.officefloor.compile.section;

import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> for a {@link Team} of a {@link ManagedObject}
 * within an {@link OfficeSection}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectTeamType {

	/**
	 * Obtains the name of the {@link ManagedObjectTeam}.
	 * 
	 * @return Name of the {@link ManagedObjectTeam}.
	 */
	String getOfficeSectionManagedObjectTeamName();

}