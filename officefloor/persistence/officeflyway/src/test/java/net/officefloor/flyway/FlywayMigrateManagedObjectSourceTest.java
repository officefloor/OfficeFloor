package net.officefloor.flyway;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Tests the {@link FlywayMigrateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayMigrateManagedObjectSourceTest {

	/**
	 * {@link FlywayExtension}.
	 */
	@RegisterExtension
	public final FlywayExtension flyway = new FlywayExtension();

	/**
	 * Validates the specification.
	 */
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(FlywayMigrateManagedObjectSource.class);
	}

	/**
	 * Validates the type.
	 */
	@Test
	public void type() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(FlywayMigrateManagedObjectSource.OBJECT_CLASS);
		type.setInput(true);
		type.addFunctionDependency(FlywayMigrateManagedObjectSource.MigrateDependencyKeys.FLYWAY.name(), Flyway.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, FlywayMigrateManagedObjectSource.class);
	}

	/**
	 * Ensure {@link Flyway} migration is working.
	 */
	@Test
	public void validateMigration() throws Exception {

		// Migrate
		this.flyway.getFlyway().migrate();

		// Ensure flyway picking up migration
		this.flyway.assertMigration();
	}

	/**
	 * Ensure setup database.
	 */
	@Test
	public void setupDatabase() throws Throwable {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((office) -> {
			OfficeArchitect architect = office.getOfficeArchitect();

			// Add data source
			this.flyway.addDataSource(architect);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Add migration
			architect.addOfficeManagedObjectSource("MIGRATE", FlywayMigrateManagedObjectSource.class.getName());
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure database migrated
			this.flyway.assertMigration();
		}
	}

	/**
	 * Ensure fail database migration.
	 */
	@Test
	public void failMigration() throws Throwable {

		// Should successfully compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((office) -> {
			OfficeArchitect architect = office.getOfficeArchitect();

			// Add data source
			this.flyway.addDataSource(architect);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Add migration
			architect.addOfficeManagedObjectSource("MIGRATE", FlywayMigrateManagedObjectSource.class.getName());
		});
		try (OfficeFloor officeFloor = compile.compileOfficeFloor()) {

			// Should not open office due to migration failure
			try {
				this.flyway.runWithFailMigration(officeFloor::openOfficeFloor);
				fail("Should not be successful");
			} catch (Exception ex) {
				assertTrue(ex.getMessage().contains("INVALID SQL CAUSING MIGRATION FAILURE"),
						"Incorrect cause " + ex.getMessage());
			}
		}
	}

}