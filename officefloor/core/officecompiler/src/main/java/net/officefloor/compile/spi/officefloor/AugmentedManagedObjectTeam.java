package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;

/**
 * Augmented {@link ManagedObjectTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedManagedObjectTeam {

	/**
	 * Obtains the name of this {@link ManagedObjectTeam}.
	 * 
	 * @return Name of this {@link ManagedObjectTeam}.
	 */
	String getManagedObjectTeamName();

	/**
	 * Indicates if the {@link ManagedObjectTeam} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}