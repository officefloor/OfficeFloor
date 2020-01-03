package net.officefloor.jdbc.decorate;

import java.sql.Connection;

/**
 * Decorator on all created {@link Connection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionDecorator {

	/**
	 * Allows decorating the {@link Connection}.
	 * 
	 * @param connection {@link Connection} to decorate.
	 * @return Decorated {@link Connection} or possibly new wrapping
	 *         {@link Connection} implementation.
	 */
	Connection decorate(Connection connection);

}