package net.officefloor.jdbc.pool;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.sql.PooledConnection;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.decorate.PooledConnectionDecorator;
import net.officefloor.jdbc.decorate.PooledConnectionDecoratorFactory;

/**
 * {@link PooledConnectionDecoratorFactory} to capture the
 * {@link PooledConnection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CapturePooledConnectionsDecoratorFactory
		implements PooledConnectionDecoratorFactory, PooledConnectionDecorator {

	public static boolean isActive = false;

	public static Deque<PooledConnection> connections = new ConcurrentLinkedDeque<>();

	/*
	 * ================== PooledConnectionDecoratorFactory ==================
	 */

	@Override
	public PooledConnectionDecorator createPooledConnectionDecorator(SourceContext context) throws Exception {
		return this;
	}

	/*
	 * ===================== PooledConnectionDecorator =======================
	 */

	@Override
	public PooledConnection decorate(PooledConnection connection) {

		// Register connection if active
		if (isActive) {
			connections.add(connection);
		}

		// Return the connection
		return connection;
	}

}