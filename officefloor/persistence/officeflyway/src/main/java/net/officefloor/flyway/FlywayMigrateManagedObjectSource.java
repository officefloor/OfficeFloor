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

				// TODO fix to Throwable
				Exception exception = (Exception) escalation;

				migration.failOpen(exception);
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