package net.officefloor.compile.spi.managedobject;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} required by the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTeam {

	/**
	 * Obtains the name of the {@link ManagedObjectTeam}.
	 * 
	 * @return Name of the {@link ManagedObjectTeam}.
	 */
	String getManagedObjectTeamName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link ManagedObjectTeam}.
	 * <p>
	 * This enables distinguishing {@link ManagedObjectTeam} instances to enable
	 * dynamic {@link Team} assignment.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code> if no qualification.
	 * @param type      Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

}