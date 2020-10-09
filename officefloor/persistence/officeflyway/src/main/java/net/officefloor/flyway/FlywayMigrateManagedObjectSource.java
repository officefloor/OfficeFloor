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

import org.flywaydb.core.Flyway;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link Flyway} migrate {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayMigrateManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * Migrate dependency keys.
	 */
	public static enum MigrateDependencyKeys {
		FLYWAY
	}

	/**
	 * Flow keys.
	 */
	public static enum FlowKeys {
		MIGRATE
	}

	/*
	 * ==================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No Specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(FlywayMigration.class);

		// Services must start up after migration
		ManagedObjectStartupCompletion migrationCompletion = mosContext.createStartupCompletion();

		// Register migration
		final String migrateFunctionName = "migrate";
		ManagedObjectFunctionBuilder<MigrateDependencyKeys, None> migrateFunction = mosContext
				.addManagedFunction(migrateFunctionName, new FlywayMigrate(migrationCompletion));
		migrateFunction.linkObject(MigrateDependencyKeys.FLYWAY,
				mosContext.addFunctionDependency(MigrateDependencyKeys.FLYWAY.name(), Flyway.class));

		// Undertake migration on start up
		mosContext.addStartupFunction(migrateFunctionName, null);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new FlywayMigrateManagedObject();
	}

	/**
	 * {@link Flyway} migrate {@link ManagedObject}.
	 */
	private static class FlywayMigrateManagedObject implements ManagedObject, FlywayMigration {

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * {@link ManagedFunction} for the {@link Flyway} migration.
	 */
	private static class FlywayMigrate extends StaticManagedFunction<MigrateDependencyKeys, None> {

		/**
		 * {@link ManagedObjectStartupCompletion} to indicate migration complete.
		 */
		private ManagedObjectStartupCompletion migrationCompletion;

		/**
		 * Instantiate.
		 * 
		 * @param migrationCompletion {@link ManagedObjectStartupCompletion} to indicate
		 *                            migration complete.
		 */
		private FlywayMigrate(ManagedObjectStartupCompletion migrationCompletion) {
			this.migrationCompletion = migrationCompletion;
		}

		/*
		 * ===================== ManagedFunction ========================
		 */

		@Override
		public void execute(ManagedFunctionContext<MigrateDependencyKeys, None> context) throws Throwable {
			try {
				// Obtain flyway
				Flyway flyway = (Flyway) context.getObject(MigrateDependencyKeys.FLYWAY);

				// Undertake migration
				flyway.migrate();

				// Successfully migrated
				this.migrationCompletion.complete();

			} catch (Throwable ex) {
				// Migration failed
				this.migrationCompletion.failOpen(ex);
			}
		}
	}

}
