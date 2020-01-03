package net.officefloor.compile.internal.structure;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;

/**
 * {@link SectionFunctionNamespace} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceNode extends Node, SectionFunctionNamespace {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Function Namespace";

	/**
	 * Initialises the {@link FunctionNamespaceNode}.
	 * 
	 * @param managedFunctionSourceClassName
	 *            {@link Class} name of the {@link ManagedFunctionSource}.
	 * @param managedFunctionSource
	 *            Optional instantiated {@link ManagedFunctionSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource);

	/**
	 * Obtains the {@link SectionNode} containing this
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @return {@link SectionNode} containing this
	 *         {@link FunctionNamespaceNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the {@link FunctionNamespaceType} for this
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @return {@link FunctionNamespaceType} for this
	 *         {@link FunctionNamespaceNode}. May be <code>null</code> if can
	 *         not load the {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType loadFunctionNamespaceType();

	/**
	 * Registers the {@link ManagedFunctionSource} as a possible MBean.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void registerAsPossibleMbean(CompileContext compileContext);

}