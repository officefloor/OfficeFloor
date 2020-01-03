package net.officefloor.compile.spi.office;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorResponsibility;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} required by the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeam extends OfficeFloorResponsibility {

	/**
	 * Obtains the name of this {@link OfficeTeam}.
	 * 
	 * @return Name of this {@link OfficeTeam}.
	 */
	String getOfficeTeamName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link OfficeTeam}.
	 * <p>
	 * This enables distinguishing {@link OfficeTeam} instances to enable, for
	 * example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

}