/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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