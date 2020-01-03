package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.ResponsibleTeam;

/**
 * {@link ResponsibleTeam} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResponsibleTeamNode extends LinkTeamNode, ResponsibleTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Responsible Team";

	/**
	 * Initialises the {@link ResponsibleTeamNode}.
	 */
	void initialise();

}