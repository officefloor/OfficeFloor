/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
