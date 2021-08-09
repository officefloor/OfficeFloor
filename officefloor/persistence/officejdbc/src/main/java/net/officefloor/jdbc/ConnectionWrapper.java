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

package net.officefloor.jdbc;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Means to obtain the {@link Connection} being wrapped.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionWrapper {

	/**
	 * {@link #getRealConnection()} {@link Method} name.
	 */
	String GET_REAL_CONNECTION_METHOD_NAME = "getRealConnection";

	/**
	 * Indicates if {@link Method} is the {@link #getRealConnection()}.
	 * 
	 * @param method {@link Method}.
	 * @return <code>true</code> if {@link #getRealConnection()}.
	 */
	static boolean isGetRealConnectionMethod(Method method) {
		return GET_REAL_CONNECTION_METHOD_NAME.equals(method.getName());
	}

	/**
	 * Obtains the {@link #getRealConnection()} {@link Method}.
	 * 
	 * @return {@link #getRealConnection()} {@link Method}.
	 */
	static Method getRealConnectionMethod() {
		try {
			return ConnectionWrapper.class.getMethod(GET_REAL_CONNECTION_METHOD_NAME);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Unable to obtain " + GET_REAL_CONNECTION_METHOD_NAME + " method");
		}
	}

	/**
	 * Obtains the &quot;real&quot; {@link Connection} from the input
	 * {@link Connection}.
	 * 
	 * @param connection {@link Connection} to inspect for the &quot;real&quot;
	 *                   {@link Connection}.
	 * @return The &quot;real&quot; {@link Connection} or <code>null</code> if none.
	 * @throws SQLException If fails to obtain the &quot;real&quot;
	 *                      {@link Connection}.
	 */
	static Connection getRealConnection(Connection connection) throws SQLException {

		// Search through the connection to obtain the real connection
		Connection conn = connection;
		while ((conn != null) && (conn instanceof ConnectionWrapper)) {
			conn = ((ConnectionWrapper) conn).getRealConnection();
		}

		// Return the real connection (or possibly null if none)
		return conn;
	}

	/**
	 * <p>
	 * Allows obtaining the actual {@link Connection}.
	 * <p>
	 * Some implementations may {@link Proxy} the {@link Connection} to source on
	 * invocation of first {@link Connection} method. Therefore, in inspecting the
	 * {@link Connection} on recycle, it will actually acquire a {@link Connection}
	 * when none was required to be recycled (creating unnecessary
	 * {@link Connection} instances).
	 * <p>
	 * This enables obtaining the &quot;real&quot; {@link Connection}.
	 * 
	 * @return The &quot;real&quot; {@link Connection}. In some implementations it
	 *         may actually be <code>null</code> as no &quot;real&quot; backing
	 *         {@link Connection} is available.
	 * @throws SQLException If fails to obtain the &quot;real&quot;
	 *                      {@link Connection}.
	 */
	Connection getRealConnection() throws SQLException;

}
