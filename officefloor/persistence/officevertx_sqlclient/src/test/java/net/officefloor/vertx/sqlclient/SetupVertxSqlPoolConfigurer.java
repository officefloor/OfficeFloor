package net.officefloor.vertx.sqlclient;

import io.vertx.sqlclient.SqlConnectOptions;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Setup {@link VertxSqlPoolConfigurer}.
 * 
 * @author Daniel Sagenschneider
 */
public class SetupVertxSqlPoolConfigurer implements VertxSqlPoolConfigurer, VertxSqlPoolConfigurerServiceFactory {

	/*
	 * ================ VertxSqlPoolConfigurerServiceFactory =================
	 */

	@Override
	public VertxSqlPoolConfigurer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= VertxSqlPoolConfigurer =========================
	 */

	@Override
	public void configure(VertxSqlPoolConfigurerContext context) throws Exception {

		// Configure connection
		SqlConnectOptions connectOptions = context.getSqlConnectOptions();
		connectOptions.setHost("localhost").setPort(AbstractDatabaseTestCase.PORT)
				.setDatabase(AbstractDatabaseTestCase.DATABASE);

		// Username to be overridden
		connectOptions.setUser("Will be overridden");

		// Configure pool
		context.getPoolOptions().setMaxSize(5);
	}

}