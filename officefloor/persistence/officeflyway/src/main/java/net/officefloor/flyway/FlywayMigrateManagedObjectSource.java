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
