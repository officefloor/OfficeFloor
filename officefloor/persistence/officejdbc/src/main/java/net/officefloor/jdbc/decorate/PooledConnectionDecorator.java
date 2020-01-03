package net.officefloor.jdbc.decorate;

import java.sql.Connection;

import javax.sql.PooledConnection;

/**
 * Decorator on all created {@link PooledConnection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface PooledConnectionDecorator {

	/**
	 * Allows decorating the {@link PooledConnection}.
	 * 
	 * @param connection {@link PooledConnection} to decorate.
	 * @return Decorated {@link PooledConnection} or possibly new wrapping
	 *         {@link Connection} implementation.
	 */
	PooledConnection decorate(PooledConnection connection);

}
