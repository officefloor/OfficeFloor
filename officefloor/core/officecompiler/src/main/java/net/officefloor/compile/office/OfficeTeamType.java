package net.officefloor.compile.office;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of a {@link Team} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamType {

	/**
	 * Obtains the name of the required {@link Team}.
	 * 
	 * @return Name of the required {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeTeam}.
	 */
	TypeQualification[] getTypeQualification();

}