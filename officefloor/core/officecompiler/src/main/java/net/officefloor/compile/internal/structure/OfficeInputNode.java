package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;

/**
 * {@link OfficeInput} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeInputNode extends LinkFlowNode, OfficeInput {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Input";

	/**
	 * Initialises the {@link OfficeInputNode}.
	 * 
	 * @param parameterType
	 *            Parameter type of {@link OfficeInput}.
	 */
	void initialise(String parameterType);

	/**
	 * Obtains the {@link OfficeInputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeInputType} or <code>null</code> if can not
	 *         determine.
	 */
	OfficeInputType loadOfficeInputType(CompileContext compileContext);

}