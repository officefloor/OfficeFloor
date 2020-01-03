package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.ManagingOffice;

/**
 * {@link ManagingOffice} node.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagingOfficeNode extends LinkOfficeNode, ManagingOffice {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managing Office";

	/**
	 * Initialises the {@link ManagingOfficeNode}.
	 */
	void initialise();

}