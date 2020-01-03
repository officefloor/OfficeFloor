package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.OfficeFloorTeamOversight;

/**
 * {@link OfficeFloorTeamOversight} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamOversightNode extends LinkTeamOversightNode, OfficeFloorTeamOversight {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Team Oversight";

	/**
	 * Initialises the {@link TeamOversightNode}.
	 */
	void initialise();

}