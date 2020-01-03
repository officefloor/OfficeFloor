package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.team.Team;

/**
 * Context for the {@link TeamAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamAugmentorContext extends SourceIssues {

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the {@link TeamType} of the {@link Team}.
	 * 
	 * @return {@link Team} of the {@link Team}.
	 */
	TeamType getTeamType();

	/**
	 * Specifies the {@link OfficeFloorTeamOversight} for the {@link Team}.
	 * 
	 * @param teamOversight {@link OfficeFloorTeamOversight}.
	 */
	void setTeamOversight(OfficeFloorTeamOversight teamOversight);

	/**
	 * Indicates if the {@link Team} already has {@link TeamOversight}.
	 * 
	 * @return <code>true</code> if the {@link Team} already has
	 *         {@link TeamOversight}.
	 */
	boolean isTeamOversight();

}