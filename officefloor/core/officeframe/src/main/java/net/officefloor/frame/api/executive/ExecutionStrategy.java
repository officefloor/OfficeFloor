package net.officefloor.frame.api.executive;

import java.util.concurrent.ThreadFactory;

/**
 * Strategy of execution.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionStrategy {

	/**
	 * Obtains the name of the {@link ExecutionStrategy}.
	 * 
	 * @return Name of the {@link ExecutionStrategy}.
	 */
	String getExecutionStrategyName();

	/**
	 * Obtains the {@link ThreadFactory} instances for the various {@link Thread}
	 * instances of this {@link ExecutionStrategy}.
	 * 
	 * @return {@link ThreadFactory} instances.
	 */
	ThreadFactory[] getThreadFactories();

}