/*-
 * #%L
 * OfficeFloor Flyway Migration
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
