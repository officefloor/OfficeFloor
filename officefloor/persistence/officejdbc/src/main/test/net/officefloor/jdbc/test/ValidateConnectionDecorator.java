package net.officefloor.jdbc.test;

import java.sql.Connection;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.jdbc.decorate.ConnectionDecorator;
import net.officefloor.jdbc.decorate.ConnectionDecoratorServiceFactory;

/**
 * {@link ConnectionDecorator} to validate the {@link Connection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateConnectionDecorator implements ConnectionDecoratorServiceFactory, ConnectionDecorator {

	/*
	 * ============= ConnectionDecoratorServiceFactory =========================
	 */

	@Override
	public ConnectionDecorator createService(ServiceContext context) throws Exception {
		return this;
	}

	/*
	 * ===================== ConnectionDecorator ===============================
	 */

	@Override
	public Connection decorate(Connection connection) {
		return ValidateConnections.addConnection(connection);
	}

}