/*-
 * #%L
 * OfficeFloor Flyway
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

package net.officefloor.flyway;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.sql.DataSource;

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
 * Tests the {@link FlywayManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayManagedObjectSourceTest {

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
		ManagedObjectLoaderUtil.validateSpecification(FlywayManagedObjectSource.class);
	}

	/**
	 * Validates the type.
	 */
	@Test
	public void type() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Flyway.class);
		type.addDependency(FlywayManagedObjectSource.DependencyKeys.DATA_SOURCE, DataSource.class, null);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, FlywayManagedObjectSource.class);
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
	public void migrate() throws Throwable {
		try (OfficeFloor officeFloor = this.compileOfficeFloor()) {

			// Migrate
			CompileOfficeFloor.invokeProcess(officeFloor, "MIGRATE.migrate", null);

			// Ensure database migrated
			this.flyway.assertMigration();
		}
	}

	/**
	 * Ensure fail migrate.
	 */
	@Test
	public void failMigrate() throws Throwable {
		try (OfficeFloor officeFloor = this.compileOfficeFloor()) {

			// Attempt migrate
			try {
				this.flyway.runWithFailMigration(
						() -> CompileOfficeFloor.invokeProcess(officeFloor, "MIGRATE.migrate", null));
				fail("Should not successfully migrate");
			} catch (Exception ex) {
				assertTrue(ex.getMessage().contains("INVALID SQL CAUSING MIGRATION FAILURE"),
						"Incorrect cause " + ex.getMessage());
			}
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
			this.flyway.addDataSource(architect);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Function to migrate
			office.addSection("MIGRATE", MigrateSection.class);
		});
		return compile.compileAndOpenOfficeFloor();
	}

	public static class MigrateSection {
		public void migrate(Flyway flyway) {
			flyway.migrate();
		}
	}

}
