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
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
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
public class FlywayMigrateManagedObjectSource
		extends AbstractManagedObjectSource<None, FlywayMigrateManagedObjectSource.FlowKeys> {

	/**
	 * Object {@link Class}.
	 */
	static final Class<?> OBJECT_CLASS = FlywayMigrateManagedObject.class;

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
	protected void loadMetaData(MetaDataContext<None, FlowKeys> context) throws Exception {
		ManagedObjectSourceContext<FlowKeys> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(OBJECT_CLASS);

		// Register migration
		final String migrateFunctionName = "migrate";
		context.addFlow(FlowKeys.MIGRATE, null);
		ManagedObjectFunctionBuilder<MigrateDependencyKeys, None> migrateFunction = mosContext
				.addManagedFunction(migrateFunctionName, new FlywayMigration());
		migrateFunction.linkObject(MigrateDependencyKeys.FLYWAY,
				mosContext.addFunctionDependency(MigrateDependencyKeys.FLYWAY.name(), Flyway.class));
		mosContext.getFlow(FlowKeys.MIGRATE).linkFunction(migrateFunctionName);
	}

	@Override
	public void start(ManagedObjectExecuteContext<FlowKeys> context) throws Exception {

		// Services must start up after migration
		ManagedObjectStartupCompletion migration = context.createStartupCompletion();

		// Register flow for migration
		context.registerStartupProcess(FlowKeys.MIGRATE, null, null, (escalation) -> {

			// Determine if failure migrating
			if (escalation != null) {
				// Failed migration, so fail start up
				migration.failOpen(escalation);
				return;
			}

			// As here, ready to start services
			migration.complete();
		});
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new FlywayMigrateManagedObject();
	}

	/**
	 * {@link Flyway} migrate {@link ManagedObject}.
	 */
	private static class FlywayMigrateManagedObject implements ManagedObject {

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * {@link ManagedFunction} for the {@link Flyway} migration.
	 */
	private static class FlywayMigration extends StaticManagedFunction<MigrateDependencyKeys, None> {

		/*
		 * ===================== ManagedFunction ========================
		 */

		@Override
		public void execute(ManagedFunctionContext<MigrateDependencyKeys, None> context) throws Throwable {

			// Obtain flyway
			Flyway flyway = (Flyway) context.getObject(MigrateDependencyKeys.FLYWAY);

			// Undertake migration
			flyway.migrate();
		}
	}

}
