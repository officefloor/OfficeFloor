package net.officefloor.flyway.test;

import org.flywaydb.core.Flyway;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.flyway.FlywayManagedObjectSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.h2.H2DataSourceManagedObjectSource;

/**
 * Abstract testing of enable clean for tests.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFlywayEnableCleanTestCase {

	/**
	 * Ensure able to undertake clean.
	 */
	protected void doClean() throws Throwable {
		try (OfficeFloor officeFloor = this.compileOfficeFloor()) {

			// Clean
			CompileOfficeFloor.invokeProcess(officeFloor, "CLEAN.clean", null);
		}
	}

	/**
	 * Return compiled {@link OfficeFloor}.
	 * 
	 * @return Compiled {@link OfficeFloor}.
	 */
	private OfficeFloor compileOfficeFloor() throws Exception {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((office) -> {
			OfficeArchitect architect = office.getOfficeArchitect();

			// Add data source
			OfficeManagedObjectSource dataSource = architect.addOfficeManagedObjectSource("DATASOURCE",
					H2DataSourceManagedObjectSource.class.getName());
			dataSource.addProperty(H2DataSourceManagedObjectSource.PROPERTY_URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
			dataSource.addProperty(H2DataSourceManagedObjectSource.PROPERTY_USER, "sa");
			dataSource.addOfficeManagedObject("DATASOURCE", ManagedObjectScope.THREAD);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Function to migrate and clean
			office.addSection("CLEAN", CleanSection.class);
		});
		return compile.compileAndOpenOfficeFloor();
	}

	public static class CleanSection {
		public void clean(Flyway flyway) {
			flyway.clean();
		}
	}

}