/*-
 * #%L
 * OfficeFloor Flyway
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.flyway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	 * Clean database for each test.
	 */
	@BeforeEach
	public void cleanDatabase() {
		FlywayTestHelper.clean();
	}

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
	 * Ensure setup database.
	 */
	@Test
	public void migrate() throws Throwable {
		try (OfficeFloor officeFloor = this.compileOfficeFloor()) {

			// Migrate
			CompileOfficeFloor.invokeProcess(officeFloor, "MIGRATE.migrate", null);

			// Ensure database migrated
			FlywayTestHelper.assertMigration();
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
				FlywayTestHelper.runWithFailMigration(
						() -> CompileOfficeFloor.invokeProcess(officeFloor, "MIGRATE.migrate", null));
				fail("Should not successfully migrate");
			} catch (Exception ex) {
				assertTrue(ex.getMessage().contains("INVALID SQL CAUSING MIGRATION FAILURE"),
						"Incorrect cause " + ex.getMessage());
			}
		}
	}

	/**
	 * Ensure can clean database.
	 */
	@Test
	public void clean() throws Throwable {
		try (OfficeFloor officeFloor = this.compileOfficeFloor()) {

			// Clean
			CompileOfficeFloor.invokeProcess(officeFloor, "CLEAN.clean", null);
			fail("Should not allow clean");
		} catch (FlywayException ex) {
			assertEquals("Unable to execute clean as it has been disabled with the 'flyway.cleanDisabled' property.",
					ex.getMessage(), "Should not be able to clean");
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
			FlywayTestHelper.addDataSource(architect);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Function to migrate and clean
			office.addSection("MIGRATE", MigrateSection.class);
			office.addSection("CLEAN", CleanSection.class);
		});
		return compile.compileAndOpenOfficeFloor();
	}

	public static class MigrateSection {
		public void migrate(Flyway flyway) {
			flyway.migrate();
		}
	}

	public static class CleanSection {
		public void clean(Flyway flyway) {
			flyway.clean();
		}
	}

}
