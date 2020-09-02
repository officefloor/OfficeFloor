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

package net.officefloor.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import junit.framework.TestCase;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * Abstract {@link Connection} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionTestCase extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorJdbcExtension}.
	 */
	private final OfficeFloorJdbcExtension jdbc = new OfficeFloorJdbcExtension();

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 */
	protected Connection getConnection() throws SQLException {
		return this.jdbc.newConnection();
	}

	/**
	 * Cleans the database.
	 * 
	 * @param connection {@link Connection}.
	 */
	protected void cleanDatabase(Connection connection) throws SQLException {
		this.jdbc.cleanDatabase(connection);
	}

	/**
	 * Loads the properties for the {@link ConnectionManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected void loadProperties(PropertyConfigurable mos) {
		this.jdbc.loadProperties(mos);
	}

	@Override
	protected void setUp() throws Exception {
		this.connection = this.jdbc.beforeEach();
	}

	@Override
	protected void tearDown() throws Exception {
		this.jdbc.afterEach();
	}

	/**
	 * Ensure:
	 * <ul>
	 * <li>connectivity test is undertaken on opening the {@link OfficeFloor}</li>
	 * <li>close the {@link DataSource} on {@link OfficeFloor} close if implements
	 * {@link AutoCloseable}</li>
	 * </ul>
	 * 
	 * @param managedObjectSource          {@link ManagedObjectSource}
	 *                                     {@link Class}.
	 * @param startupAdditionalConnections Additional {@link Connection} instances
	 *                                     used at start up (beyond connectivity
	 *                                     test).
	 */
	public void doDataSourceManagementTest(Class<? extends ManagedObjectSource<?, ?>> managedObjectSource,
			int startupAdditionalConnections) throws Throwable {
		this.jdbc.doDataSourceManagementTest(managedObjectSource, startupAdditionalConnections);
	}

	/**
	 * Mock {@link DataSourceFactory}.
	 */
	public static class HikariDataSourceFactory implements DataSourceFactory {

		private static HikariDataSource dataSource;

		@Override
		public DataSource createDataSource(SourceContext context) throws Exception {
			dataSource = new HikariDataSource();
			dataSource.setJdbcUrl("jdbc:h2:mem:test");
			return dataSource;
		}
	}

}
