package net.officefloor.compile.internal.structure;

import net.officefloor.compile.team.TeamType;

/**
 * Visits each {@link TeamNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamVisitor {

	/**
	 * Visits the {@link TeamNode}.
	 * 
	 * @param teamType       {@link TeamType}.
	 * @param teamNode       {@link TeamNode}.
	 * @param compileContext {@link CompileContext}.
	 */
	void visit(TeamType teamType, TeamNode teamNode, CompileContext compileContext);

}