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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		ManagedObjectLoaderUtil.validateSpecification(FlywayMigrateManagedObjectSource.class);
	}

	/**
	 * Validates the type.
	 */
	@Test
	public void type() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(FlywayMigration.class);
		type.addFunctionDependency(FlywayMigrateManagedObjectSource.MigrateDependencyKeys.FLYWAY.name(), Flyway.class,
				null);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, FlywayMigrateManagedObjectSource.class);
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
			FlywayTestHelper.addDataSource(architect);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Add migration
			architect.addOfficeManagedObjectSource("MIGRATE", FlywayMigrateManagedObjectSource.class.getName())
					.addOfficeManagedObject("MIGRATE", ManagedObjectScope.THREAD);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure database migrated
			FlywayTestHelper.assertMigration();
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
			FlywayTestHelper.addDataSource(architect);

			// Add flyway
			architect.addOfficeManagedObjectSource("FLYWAY", FlywayManagedObjectSource.class.getName())
					.addOfficeManagedObject("FLYWAY", ManagedObjectScope.THREAD);

			// Add migration
			architect.addOfficeManagedObjectSource("MIGRATE", FlywayMigrateManagedObjectSource.class.getName())
					.addOfficeManagedObject("MIGRATE", ManagedObjectScope.THREAD);
		});
		try (OfficeFloor officeFloor = compile.compileOfficeFloor()) {

			// Should not open office due to migration failure
			try {
				FlywayTestHelper.runWithFailMigration(officeFloor::openOfficeFloor);
				fail("Should not be successful");
			} catch (Exception ex) {
				assertTrue(ex.getMessage().contains("INVALID SQL CAUSING MIGRATION FAILURE"),
						"Incorrect cause " + ex.getMessage());
			}
		}
	}

}
