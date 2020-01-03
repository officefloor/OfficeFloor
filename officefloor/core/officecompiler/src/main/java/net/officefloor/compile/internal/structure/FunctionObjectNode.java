package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.office.AugmentedFunctionObject;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link FunctionObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionObjectNode extends LinkObjectNode, AugmentedFunctionObject, FunctionObject {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Function Object";

	/**
	 * Initialises the {@link FunctionObjectNode}.
	 */
	void initialise();

	/**
	 * Indicates if this {@link FunctionObject} is a parameter to the
	 * {@link ManagedFunction}.
	 * 
	 * @return <code>true</code> if this {@link FunctionObject} is a parameter
	 *         to the {@link ManagedFunction}.
	 */
	boolean isParameter();

	/**
	 * Loads the {@link ObjectDependencyType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link ObjectDependencyType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	ObjectDependencyType loadObjectDependencyType(CompileContext compileContext);

}