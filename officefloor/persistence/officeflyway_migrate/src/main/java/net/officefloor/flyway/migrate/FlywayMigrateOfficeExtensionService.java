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
		officeArchitect.addOfficeManagedObjectSource("_FLYWAY_MIGRATE_",
				FlywayMigrateManagedObjectSource.class.getName());
	}

}