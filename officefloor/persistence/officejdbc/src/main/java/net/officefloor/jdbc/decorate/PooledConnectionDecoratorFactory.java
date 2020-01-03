package net.officefloor.jdbc.decorate;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for the creation of a {@link PooledConnectionDecorator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface PooledConnectionDecoratorFactory {

	/**
	 * Creates the {@link PooledConnectionDecorator}.
	 * 
	 * @param context {@link SourceContext} to configure the
	 *                {@link PooledConnectionDecorator}.
	 * @return {@link PooledConnectionDecorator}.
	 * @throws Exception If fails to create the {@link PooledConnectionDecorator}.
	 */
	PooledConnectionDecorator createPooledConnectionDecorator(SourceContext context) throws Exception;

}