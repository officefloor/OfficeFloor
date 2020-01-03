package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.DependentObjectType;

/**
 * {@link Node} of the object fulfilling a dependency.
 *
 * @author Daniel Sagenschneider
 */
public interface DependentObjectNode extends LinkObjectNode {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Dependent Object";

	/**
	 * Loads the {@link DependentObjectType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link DependentObjectType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	DependentObjectType loadDependentObjectType(CompileContext compileContext);

}