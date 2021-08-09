/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
