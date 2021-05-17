package net.officefloor.vertx.sqlclient;

import java.util.function.Consumer;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.vertx.OfficeFloorVertx;

/**
 * {@link ManagedObjectSource} for {@link Vertx} {@link Pool}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxSqlPoolManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * {@link Property} name for host.
	 */
	public static final String PROPERTY_HOST = "host";

	/**
	 * {@link Property} name for port.
	 */
	public static final String PROPERTY_PORT = "port";

	/**
	 * {@link Property} name for database.
	 */
	public static final String PROPERTY_DATABASE = "database";

	/**
	 * {@link Property} name for username.
	 */
	public static final String PROPERTY_USERNAME = "username";

	/**
	 * {@link Property} name for password.
	 */
	public static final String PROPERTY_PASSWORD = "password";

	/**
	 * Loads the {@link Property}.
	 * 
	 * @param propertyName      Name of {@link Property}.
	 * @param mosContext        {@link ManagedObjectSourceContext}.
	 * @param loadPropertyValue Loads the {@link Property} value.
	 */
	private static void loadProperty(String propertyName, ManagedObjectSourceContext<None> mosContext,
			Consumer<String> loadPropertyValue) {
		String propertyValue = mosContext.getProperty(propertyName, null);
		if (propertyValue != null) {
			loadPropertyValue.accept(propertyValue);
		}
	}

	/**
	 * {@link Pool}.
	 */
	private Pool pool;

	/**
	 * Obtains the {@link Pool}.
	 * 
	 * @return {@link Pool}.
	 */
	public Pool getPool() {
		return this.pool;
	}

	/*
	 * ===================== ManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Provide meta-data
		context.setObjectClass(Pool.class);

		// Only connect if not type
		if (mosContext.isLoadingType()) {
			return;
		}

		// Create the options
		SqlConnectOptions connectOptions = new SqlConnectOptions();
		PoolOptions poolOptions = new PoolOptions();

		// Provide configuration
		for (VertxSqlPoolConfigurer configurer : mosContext
				.loadOptionalServices(VertxSqlPoolConfigurerServiceFactory.class)) {
			configurer.configure(new VertxSqlPoolConfigurerContext() {

				@Override
				public SqlConnectOptions getSqlConnectOptions() {
					return connectOptions;
				}

				@Override
				public PoolOptions getPoolOptions() {
					return poolOptions;
				}
			});
		}

		// Load the properties
		loadProperty(PROPERTY_HOST, mosContext, (value) -> connectOptions.setHost(value));
		loadProperty(PROPERTY_PORT, mosContext, (value) -> connectOptions.setPort(Integer.parseInt(value)));
		loadProperty(PROPERTY_DATABASE, mosContext, (value) -> connectOptions.setDatabase(value));
		loadProperty(PROPERTY_USERNAME, mosContext, (value) -> connectOptions.setUser(value));
		loadProperty(PROPERTY_PASSWORD, mosContext, (value) -> connectOptions.setPassword(value));

		// Configure the pool
		Vertx vertx = OfficeFloorVertx.getVertx();
		this.pool = Pool.pool(vertx, connectOptions, poolOptions);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================== ManagedObject =========================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.pool;
	}

}