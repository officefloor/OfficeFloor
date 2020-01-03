package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * {@link OfficeTeam} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamNode extends LinkTeamNode, OfficeTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Team";

	/**
	 * Initialises the {@link OfficeTeamNode}.
	 */
	void initialise();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeTeam}.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * Loads the {@link OfficeTeamType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeTeamType} or <code>null</code> with issues reported
	 *         to the {@link CompilerIssues}.
	 */
	OfficeTeamType loadOfficeTeamType(CompileContext compileContext);

}