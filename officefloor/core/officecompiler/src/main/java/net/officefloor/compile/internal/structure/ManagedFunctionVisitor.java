package net.officefloor.compile.internal.structure;

import net.officefloor.compile.managedfunction.ManagedFunctionType;

/**
 * Visits each {@link ManagedFunctionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionVisitor {

	/**
	 * Visits the {@link ManagedFunctionNode}.
	 * 
	 * @param managedFunctionType
	 *            {@link ManagedFunctionType}.
	 * @param managedFunctionNode
	 *            {@link ManagedFunctionNode}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void visit(ManagedFunctionType<?, ?> managedFunctionType, ManagedFunctionNode managedFunctionNode,
			CompileContext compileContext);

}