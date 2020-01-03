package net.officefloor.compile.team;

import net.officefloor.frame.api.team.Team;

/**
 * <p>
 * <code>Type definition</code> of a {@link Team}.
 * <p>
 * All {@link Team} instances implement the same interface. However, they differ
 * in their characteristics which is internal to the {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamType {

	/**
	 * Flags that the {@link Team} size must be specified.
	 * 
	 * @return <code>true</code> should the {@link Team} size be required.
	 */
	boolean isRequireTeamSize();

}