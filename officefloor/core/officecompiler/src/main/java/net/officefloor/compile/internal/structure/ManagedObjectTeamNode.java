package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.office.OfficeManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectTeam;

/**
 * {@link OfficeTeam} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTeamNode extends LinkTeamNode, AugmentedManagedObjectTeam, OfficeSectionManagedObjectTeam,
		OfficeManagedObjectTeam, OfficeFloorManagedObjectTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Source Team";

	/**
	 * Initialises the {@link ManagedObjectTeamNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link OfficeSectionManagedObjectTeamType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeSectionManagedObjectTeamType} or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues}.
	 */
	OfficeSectionManagedObjectTeamType loadOfficeSectionManagedObjectTeamType(CompileContext compileContext);

}