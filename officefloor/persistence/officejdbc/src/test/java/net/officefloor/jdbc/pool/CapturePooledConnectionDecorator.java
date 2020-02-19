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

package net.officefloor.jdbc.pool;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.sql.PooledConnection;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.jdbc.decorate.PooledConnectionDecorator;
import net.officefloor.jdbc.decorate.PooledConnectionDecoratorServiceFactory;

/**
 * {@link PooledConnectionDecorator} to capture the {@link PooledConnection}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CapturePooledConnectionDecorator
		implements PooledConnectionDecoratorServiceFactory, PooledConnectionDecorator {

	public static boolean isActive = false;

	public static Deque<PooledConnection> connections = new ConcurrentLinkedDeque<>();

	/*
	 * ================== PooledConnectionDecoratorFactory ==================
	 */

	@Override
	public PooledConnectionDecorator createService(ServiceContext context) throws Exception {
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
