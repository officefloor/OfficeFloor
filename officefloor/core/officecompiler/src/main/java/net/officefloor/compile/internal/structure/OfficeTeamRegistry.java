package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * Factory for the creation of an {@link OfficeTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamRegistry {

	/**
	 * Obtains the {@link OfficeTeamNode} instances.
	 * 
	 * @return {@link OfficeTeamNode} instances.
	 */
	OfficeTeamNode[] getOfficeTeams();

	/**
	 * <p>
	 * Creates the {@link OfficeTeamNode}.
	 * <p>
	 * The name of the {@link OfficeTeamNode} may be adjusted to ensure
	 * uniqueness.
	 * 
	 * @param officeTeamName
	 *            {@link OfficeTeam} name.
	 * @return {@link OfficeTeamNode}.
	 */
	OfficeTeamNode createOfficeTeam(String officeTeamName);

}