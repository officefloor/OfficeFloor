package net.officefloor.jdbc.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.postgresql.ds.PGSimpleDataSource;

import com.zaxxer.hikari.HikariDataSource;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.test.OfficeFrameTestCase.UsesDockerTest;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule.Configuration;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;

/**
 * Tests the {@link PostgreSqlConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class PostgreSqlJdbcTest extends AbstractJdbcTestCase {

	/**
	 * Port to run PostgreSql.
	 */
	private static final int PORT = 5433;

	/**
	 * Username to connect to PostgreSql.
	 */
	private static final String USERNAME = "testuser";

	/**
	 * Password to connect to PostgreSql.
	 */
	private static final String PASSWORD = "testpassword";

	/**
	 * {@link PostgreSqlRule} to run PostgreSql.
	 */
	private static PostgreSqlRule server = new PostgreSqlRule(
			new Configuration().port(PORT).username(USERNAME).password(PASSWORD));

	/**
	 * Manage PostgreSql before/after class (rather than each test) to improve
	 * performance.
	 */
	public static Test suite() {
		return new TestSetup(new TestSuite(PostgreSqlJdbcTest.class)) {

			protected void setUp() throws Exception {
				if (isSkipTestsUsingDocker()) {
					return;
				}
				server.startPostgreSql();
			}

			protected void tearDown() throws Exception {
				if (isSkipTestsUsingDocker()) {
					return;
				}
				server.stopPostgreSql();
			}
		};
	}

	@Override
	protected Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass() {
		return PostgreSqlConnectionManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return PostgreSqlReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadConnectionProperties(PropertyConfigurable mos) {
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PORT, String.valueOf(PORT));
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_USER, USERNAME);
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PASSWORD, PASSWORD);
	}

	@Override
	protected void loadOptionalConnectionSpecification(Properties properties) {
		properties.setProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PORT, "5433");
	}

	@Override
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		new PGSimpleDataSource();
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, HikariDataSource.class.getName());
		mos.addProperty("jdbcUrl", "jdbc:postgresql://localhost:" + String.valueOf(PORT) + "/");
		mos.addProperty("username", USERNAME);
		mos.addProperty("password", PASSWORD);
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeQuery("SELECT * FROM information_schema.tables");
			statement.executeUpdate("DROP TABLE IF EXISTS OFFICE_FLOOR_JDBC_TEST");
		}
		if (!connection.getAutoCommit()) {
			connection.commit();
		}
	}

}