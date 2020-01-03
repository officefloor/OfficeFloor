package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.spi.office.OfficeOutput;

/**
 * {@link OfficeOutput} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeOutputNode extends LinkFlowNode, OfficeOutput {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Output";

	/**
	 * Initialises the {@link OfficeOutputNode}.
	 * 
	 * @param argumentType
	 *            Argument type from this {@link OfficeOutput}.
	 */
	void initialise(String argumentType);

	/**
	 * Loads the {@link OfficeOutputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeOutputType} or <code>null</code> if can not
	 *         determine.
	 */
	OfficeOutputType loadOfficeOutputType(CompileContext compileContext);

}