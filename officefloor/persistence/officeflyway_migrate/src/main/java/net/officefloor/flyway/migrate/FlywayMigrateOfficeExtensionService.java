/*-
 * #%L
 * OfficeFloor Flyway Migration
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

package net.officefloor.flyway.migrate;

import org.flywaydb.core.Flyway;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.flyway.FlywayManagedObjectSource;
import net.officefloor.flyway.FlywayMigrateManagedObjectSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link OfficeExtensionService} to undertake {@link Flyway} migration.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayMigrateOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * ================ OfficeExtensionServiceFactory ====================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== OfficeExtensionService ========================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Register Flyway
		officeArchitect.addOfficeManagedObjectSource("_FLYWAY_", FlywayManagedObjectSource.class.getName())
				.addOfficeManagedObject("_FLYWAY_", ManagedObjectScope.THREAD);

		// Undertake migration
		officeArchitect
				.addOfficeManagedObjectSource("_FLYWAY_MIGRATE_", FlywayMigrateManagedObjectSource.class.getName())
				.addOfficeManagedObject("_FLYWAY_MIGRATE_", ManagedObjectScope.THREAD);
	}

}
