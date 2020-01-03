package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of a {@link Team} required by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTeamType {

	/**
	 * Obtains the name to identify requirement of a {@link Team}.
	 * 
	 * @return Name to identify requirement of a {@link Team}.
	 */
	String getTeamName();

}