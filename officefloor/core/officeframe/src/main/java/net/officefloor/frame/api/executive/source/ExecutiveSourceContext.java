package net.officefloor.frame.api.executive.source;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link ExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveSourceContext extends SourceContext {

	/**
	 * Creates the underlying {@link ThreadFactory} that should be used for
	 * {@link ExecutionStrategy} instances.
	 * 
	 * @param executionStrategyName Name of the {@link ExecutionStrategy} to
	 *                              associate {@link Thread} names to the
	 *                              {@link ExecutionStrategy}.
	 * @param executive             {@link Executive}.
	 * @return {@link ThreadFactory} to use for {@link ExecutionStrategy} instances.
	 */
	ThreadFactory createThreadFactory(String executionStrategyName, Executive executive);

}