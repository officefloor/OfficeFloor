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

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * Read-only {@link Connection} {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReadOnlyConnectionManagedObjectSource extends AbstractConnectionManagedObjectSource
		implements ManagedObject {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(ReadOnlyConnectionManagedObjectSource.class.getName());

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * {@link Connection}.
	 */
	private volatile Connection connection;

	/**
	 * Wrapped {@link Connection}.
	 */
	private volatile Connection wrappedConnection;

	/*
	 * ================== AbstractConnectionManagedObjectSource ==================
	 */

	@Override
	protected void loadFurtherMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain class loader for wrapping connection
		this.classLoader = mosContext.getClassLoader();

		// Obtain the data source
		DataSourceFactory dataSourceFactory = this.getDataSourceFactory(mosContext);
		this.dataSource = dataSourceFactory.createDataSource(mosContext);

		// Validate connectivity
		this.setConnectivity(() -> this.dataSource.getConnection());
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Create the re-usable connection
		this.connection = this.dataSource.getConnection();
		this.connection.setAutoCommit(true);
		this.connection.setReadOnly(true);

		// Wrap connection to avoid changing
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(this.classLoader);
		if (compiler == null) {

			// Fall back to proxy
			this.wrappedConnection = (Connection) Proxy.newProxyInstance(this.classLoader,
					new Class[] { Connection.class }, (object, method, args) -> {

						// Ensure not invoke specific connection methods
						switch (method.getName()) {
						case "setReadOnly":
							throw new SQLException("Connection is re-used read-only so can not be changed");
						case "close":
							return null; // no operation methods
						}

						// Undertake connection method
						return this.connection.getClass().getMethod(method.getName(), method.getParameterTypes())
								.invoke(this.connection, args);
					});

		} else {
			// Use compiled wrapper
			Class<?> wrapperClass = compiler.addWrapper(Connection.class, (wrapperContext) -> {
				switch (wrapperContext.getMethod().getName()) {
				case "setReadOnly":
					wrapperContext.write("    throw new " + compiler.getSourceName(SQLException.class)
							+ "(\"Connection is re-used read-only so can not be changed\");");
					break;
				case "close":
					wrapperContext.write(""); // no operation methods
				}
			}).compile();
			this.wrappedConnection = (Connection) wrapperClass.getConstructor(Connection.class)
					.newInstance(this.connection);
		}
	}

	@Override
	public void stop() {
		try {
			this.connection.close();
		} catch (SQLException ex) {
			LOGGER.log(Level.WARNING, "Failed to close read-only connection", ex);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	@Override
	public Object getObject() throws Throwable {
		return this.wrappedConnection;
	}

}