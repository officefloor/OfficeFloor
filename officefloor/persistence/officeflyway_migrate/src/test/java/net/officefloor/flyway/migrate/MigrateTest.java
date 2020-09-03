package net.officefloor.flyway.migrate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.h2.H2DataSourceManagedObjectSource;

/**
 * Ensures automatically migrates.
 * 
 * @author Daniel Sagenschneider
 */
public class MigrateTest {

	/**
	 * Ensure can migrate.
	 */
	@Test
	public void migrate() throws Exception {

		// Database configuration
		final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
		final String USERNAME = "sa";
		final String PASSWORD = "";

		// Compile and open
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((office) -> {

			// Add the data source
			OfficeManagedObjectSource dataSource = office.getOfficeArchitect()
					.addOfficeManagedObjectSource("DATASOURCE", H2DataSourceManagedObjectSource.class.getName());
			dataSource.addProperty(H2DataSourceManagedObjectSource.PROPERTY_URL, JDBC_URL);
			dataSource.addProperty(H2DataSourceManagedObjectSource.PROPERTY_USER, USERNAME);
			dataSource.addOfficeManagedObject("DATASOURCE", ManagedObjectScope.THREAD);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure database setup
			try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
				ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM FLYWAY_MIGRATE");
				assertTrue(resultSet.next(), "Should have row");
				assertEquals("MIGRATED", resultSet.getString("MESSAGE"), "Incorrect migrate row");
			}
		}
	}

}