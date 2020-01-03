package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;

/**
 * Sources the {@link FunctionNamespaceType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionSource {

	/**
	 * <p>
	 * Obtains the {@link ManagedFunctionSourceSpecification} for this
	 * {@link ManagedFunctionSource}.
	 * <p>
	 * This enables the {@link ManagedFunctionSourceContext} to be populated with
	 * the necessary details as per this {@link ManagedFunctionSourceSpecification}
	 * in loading the {@link FunctionNamespaceType}.
	 * 
	 * @return {@link ManagedFunctionSourceSpecification}.
	 */
	ManagedFunctionSourceSpecification getSpecification();

	/**
	 * Sources the {@link FunctionNamespaceType} by populating it via the input
	 * {@link FunctionNamespaceBuilder}.
	 * 
	 * @param functionNamespaceTypeBuilder
	 *            {@link FunctionNamespaceBuilder} to be populated with the
	 *            <code>type definition</code> of the {@link ManagedFunctionSource}.
	 * @param context
	 *            {@link ManagedFunctionSourceContext} to source details to populate
	 *            the {@link FunctionNamespaceBuilder}.
	 * @throws Exception
	 *             If fails to populate the {@link FunctionNamespaceBuilder}.
	 */
	void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception;

}