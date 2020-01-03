package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeStart;

/**
 * {@link OfficeStart} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeStartNode extends LinkFlowNode, OfficeStart {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Start";

	/**
	 * Initialises the {@link OfficeStartNode}.
	 */
	void initialise();

}