package net.officefloor.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import com.zaxxer.hikari.HikariDataSource;

import junit.framework.TestCase;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.ValidateConnectionDecoratorFactory;

/**
 * Abstract {@link Connection} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionTestCase extends OfficeFrameTestCase {

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 */
	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:mem:test");
	}

	/**
	 * Cleans the database.
	 * 
	 * @param connection {@link Connection}.
	 */
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

	/**
	 * Loads the properties for the {@link ConnectionManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected void loadProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", "jdbc:h2:mem:test");
	}

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

	@Override
	protected void setUp() throws Exception {

		// Create the connection
		this.connection = this.getConnection();

		// Clean database for testing
		this.cleanDatabase(this.connection);

		// Create table for testing
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE TEST ( ID INT, NAME VARCHAR(255) )");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.connection != null) {
			this.connection.close();
		}
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

		// Obtain connection count to setup test
		int setupCount = ValidateConnectionDecoratorFactory.getConnectionsRegisteredCount();

		// Open the OfficeFloor
		HikariDataSourceFactory.dataSource = null;
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					managedObjectSource.getName());
			mos.addProperty(ConnectionManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
					HikariDataSourceFactory.class.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);
		});
		OfficeFloor officeFloor = compiler.compileOfficeFloor();

		// Ensure no connections created and pool open
		assertEquals("Compiling should not open connection", setupCount,
				ValidateConnectionDecoratorFactory.getConnectionsRegisteredCount());
		assertFalse("DataSource should be open", HikariDataSourceFactory.dataSource.isClosed());

		// Open the OfficeFloor (should increment connection for connectivity test)
		officeFloor.openOfficeFloor();
		assertEquals("Should use connection for connectivity test", setupCount + 1 + startupAdditionalConnections,
				ValidateConnectionDecoratorFactory.getConnectionsRegisteredCount());
		assertFalse("DataSource should still be open", HikariDataSourceFactory.dataSource.isClosed());

		// Should close DataSource if implements AutoCloseable
		assertTrue("Hikari DataSource should be closeable",
				HikariDataSourceFactory.dataSource instanceof AutoCloseable);
		officeFloor.closeOfficeFloor();
		assertTrue("Hikari DataSource should be closed", HikariDataSourceFactory.dataSource.isClosed());
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