package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeam extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeFloorTeam}.
	 * 
	 * @return Name of this {@link OfficeFloorTeam}.
	 */
	String getOfficeFloorTeamName();

	/**
	 * Specifies the size of the {@link Team}.
	 * 
	 * @param teamSize Size of the {@link Team}.
	 */
	void setTeamSize(int teamSize);

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link OfficeFloorTeam}.
	 * <p>
	 * This enables distinguishing {@link OfficeFloorTeam} instances to enable, for
	 * example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code> if no qualification.
	 * @param type      Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

}