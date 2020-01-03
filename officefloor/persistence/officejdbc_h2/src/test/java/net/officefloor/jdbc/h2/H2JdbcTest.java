package net.officefloor.jdbc.h2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;

/**
 * Tests the {@link H2ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2JdbcTest extends AbstractJdbcTestCase {

	private static final String JDBC_URL = "jdbc:h2:mem:test";

	@Override
	protected Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass() {
		return H2ConnectionManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return H2ReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadConnectionProperties(PropertyConfigurable mos) {
		mos.addProperty("url", JDBC_URL);
		mos.addProperty("user", "test");
		mos.addProperty("password", "test");
	}

	@Override
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, HikariDataSource.class.getName());
		mos.addProperty("jdbcUrl", JDBC_URL);
		mos.addProperty("username", "test");
		mos.addProperty("password", "test");
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

}