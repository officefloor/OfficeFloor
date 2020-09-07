package net.officefloor.test;

import net.officefloor.compile.state.autowire.AutoWireStateManager;

/**
 * Context for the {@link TestDependencyService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestDependencyServiceContext {

	/**
	 * Obtains the qualifier of required dependency.
	 * 
	 * @return Qualifier of required dependency. May be <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the type of required dependency.
	 * 
	 * @return Type of required dependency. May be <code>null</code>.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link AutoWireStateManager}.
	 * 
	 * @return {@link AutoWireStateManager}.
	 */
	AutoWireStateManager getStateManager();

	/**
	 * Obtains the load timeout for the {@link AutoWireStateManager}.
	 * 
	 * @return Load timeout for the {@link AutoWireStateManager}.
	 */
	long getLoadTimeout();

}