package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Registry of the {@link ManagedFunctionNode} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionRegistry {

	/**
	 * <p>
	 * Adds an initialised {@link ManagedFunctionNode} to the registry.
	 * <p>
	 * Should an {@link ManagedFunctionNode} already be added by the name, then an
	 * issue is reported to the {@link CompilerIssue}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunctionNode}.
	 * @param functionTypeName
	 *            Type name of the {@link ManagedFunction} within the
	 *            {@link ManagedFunctionSource}.
	 * @param functionNamespaceNode
	 *            Parent {@link FunctionNamespaceNode}.
	 * @return Initialised {@link ManagedFunctionNode} by the name.
	 */
	ManagedFunctionNode addManagedFunctionNode(String functionName, String functionTypeName,
			FunctionNamespaceNode functionNamespaceNode);

}