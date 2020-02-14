package net.officefloor.jdbc.test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.PooledConnection;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.jdbc.decorate.PooledConnectionDecorator;
import net.officefloor.jdbc.decorate.PooledConnectionDecoratorServiceFactory;
import net.officefloor.jdbc.test.ValidateConnections.PooledConnectionClosed;

/**
 * {@link PooledConnectionDecorator} to validate the {@link PooledConnection}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidatePooledConnectionDecorator
		implements PooledConnectionDecorator, PooledConnectionDecoratorServiceFactory {

	/*
	 * =============== PooledConnectionDecoratorFactory =========================
	 */

	@Override
	public PooledConnectionDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== PooledConnectionDecorator ==============================
	 */

	@Override
	public PooledConnection decorate(PooledConnection connection) {

		// Wrap connection to determine if closed
		AtomicBoolean isClosed = new AtomicBoolean(false);
		PooledConnection wrapped = (PooledConnection) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class[] { PooledConnection.class, PooledConnectionClosed.class }, (object, method, args) -> {

					// Handle whether closed
					switch (method.getName()) {
					case "close":
						isClosed.set(true);
						break; // carry on to close connection

					case "isClosed":
						return isClosed.get();

					default:
						break;
					}

					// Invoke the method
					return connection.getClass().getMethod(method.getName(), method.getParameterTypes())
							.invoke(connection, args);
				});

		// Load wrapped connection (so can determine when closed)
		return ValidateConnections.addConnection(wrapped);
	}

}