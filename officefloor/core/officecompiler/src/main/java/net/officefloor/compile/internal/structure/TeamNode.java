package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * {@link OfficeFloorTeam} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamNode extends LinkTeamNode, LinkTeamOversightNode, OfficeFloorTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Team";

	/**
	 * Initialises the {@link TeamNode}.
	 * 
	 * @param teamSourceClassName Class name of the {@link TeamSource}.
	 * @param teamSource          Optional instantiated {@link TeamSource}. May be
	 *                            <code>null</code>.
	 */
	void initialise(String teamSourceClassName, TeamSource teamSource);

	/**
	 * Sources the {@link Team}.
	 * 
	 * @param teamVisitor    {@link TeamVisitor}.
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the {@link Team}.
	 *         <code>false</code> if failed to source, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceTeam(TeamVisitor teamVisitor, CompileContext compileContext);

	/**
	 * Indicates if the {@link TeamOversight} has been linked.
	 * 
	 * @return <code>true</code> if {@link TeamOversight} linked.
	 */
	boolean isTeamOversight();

	/**
	 * Loads the {@link TeamType} for the {@link TeamSource}.
	 * 
	 * @return {@link TeamType} or <code>null</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	TeamType loadTeamType();

	/**
	 * Loads the {@link OfficeFloorTeamSourceType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeFloorTeamSourceType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(CompileContext compileContext);

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeFloorTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeFloorTeam}.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * Builds the {@link Team} for this {@link TeamNode}.
	 * 
	 * @param builder        {@link OfficeFloorBuilder}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildTeam(OfficeFloorBuilder builder, CompileContext compileContext);

}