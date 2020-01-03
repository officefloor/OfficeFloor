package net.officefloor.compile.managedfunction;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;

/**
 * <code>Type definition</code> of a {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceType {

	/**
	 * Obtains the {@link ManagedFunctionType} definitions.
	 * 
	 * @return {@link ManagedFunctionType} definitions.
	 */
	ManagedFunctionType<?, ?>[] getManagedFunctionTypes();

}