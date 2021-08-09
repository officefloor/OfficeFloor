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
import java.sql.SQLException;

import javax.sql.DataSource;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Means to obtain the {@link DataSource} being wrapped.
 *
 * @author Daniel Sagenschneider
 */
public interface DataSourceWrapper {

	/**
	 * {@link #getRealDataSource()} {@link Method} name.
	 */
	String GET_REAL_DATA_SOURCE_METHOD_NAME = "getRealDataSource";

	/**
	 * Indicates if {@link Method} is the {@link #getRealDataSource()}.
	 * 
	 * @param method {@link Method}.
	 * @return <code>true</code> if {@link #getRealDataSource()}.
	 */
	static boolean isGetRealDataSourceMethod(Method method) {
		return GET_REAL_DATA_SOURCE_METHOD_NAME.equals(method.getName());
	}

	/**
	 * Obtains the {@link #getRealDataSource()} {@link Method}.
	 * 
	 * @return {@link #getRealDataSource()} {@link Method}.
	 */
	static Method getRealDataSourceMethod() {
		try {
			return ConnectionWrapper.class.getMethod(GET_REAL_DATA_SOURCE_METHOD_NAME);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Unable to obtain " + GET_REAL_DATA_SOURCE_METHOD_NAME + " method");
		}
	}

	/**
	 * Obtains the &quot;real&quot; {@link DataSource} from the input
	 * {@link DataSource}.
	 * 
	 * @param dataSource {@link DataSource} to inspect for the &quot;real&quot;
	 *                   {@link DataSource}.
	 * @return The &quot;real&quot; {@link DataSource}.
	 * @throws SQLException If fails to obtain the &quot;real&quot;
	 *                      {@link DataSource}.
	 */
	static DataSource getRealDataSource(DataSource dataSource) throws SQLException {

		// Search through the Data Sources to obtain the real Data Sources
		DataSource prev = dataSource;
		DataSource real = dataSource;
		while ((real != null) && (real instanceof DataSourceWrapper)) {
			prev = real;
			real = ((DataSourceWrapper) real).getRealDataSource();
		}
		return (real != null) ? real : prev;
	}

	/**
	 * <p>
	 * Allows obtaining the actual {@link DataSource}.
	 * <p>
	 * Some implementations may {@link Proxy} the {@link DataSource}. Therefore, in
	 * inspecting the {@link DataSource} on closing {@link OfficeFloor}, it will
	 * incorrectly determine if the {@link DataSource} is {@link AutoCloseable}.
	 * <p>
	 * This enables obtaining the &quot;real&quot; {@link DataSource}.
	 * 
	 * @return The &quot;real&quot; {@link DataSource}. In some implementations it
	 *         may actually be <code>null</code> as no &quot;real&quot; backing
	 *         {@link DataSource} is available.
	 * @throws SQLException If fails to obtain the &quot;real&quot;
	 *                      {@link DataSource}.
	 */
	DataSource getRealDataSource() throws SQLException;

}
