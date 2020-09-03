package net.officefloor.flyway;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for {@link Flyway}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayManagedObjectSource
		extends AbstractManagedObjectSource<FlywayManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		DATA_SOURCE
	}

	/**
	 * {@link FlywayConfigurer} instances.
	 */
	private FlywayConfigurer[] flywayConfigurers;

	/*
	 * ===================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Provide meta data
		context.setObjectClass(Flyway.class);
		context.setManagedObjectClass(FlywayManagedObject.class);
		context.addDependency(DependencyKeys.DATA_SOURCE, DataSource.class);

		// Load the flyway configurers
		List<FlywayConfigurer> configurers = new LinkedList<>();
		for (FlywayConfigurer configurer : mosContext.loadOptionalServices(FlywayConfigurerServiceFactory.class)) {
			configurers.add(configurer);
		}
		this.flywayConfigurers = configurers.toArray(new FlywayConfigurer[configurers.size()]);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new FlywayManagedObject();
	}

	/**
	 * {@link Flyway} {@link ManagedObject}.
	 */
	private class FlywayManagedObject implements CoordinatingManagedObject<DependencyKeys> {

		/**
		 * {@link Flyway}.
		 */
		private Flyway flyway;

		/*
		 * ===================== ManagedObject ==========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {

			// Obtain the dependency
			DataSource dataSource = (DataSource) registry.getObject(DependencyKeys.DATA_SOURCE);

			// Create the flyway configurers
			FluentConfiguration configuration = Flyway.configure().dataSource(dataSource);
			for (FlywayConfigurer configurer : FlywayManagedObjectSource.this.flywayConfigurers) {
				configurer.configure(configuration);
			}

			// Setup flyway
			this.flyway = configuration.load();
		}

		@Override
		public Object getObject() throws Throwable {
			return this.flyway;
		}
	}

}