package net.officefloor.vertx.sqlclient;

import io.vertx.sqlclient.Pool;

/**
 * Configures the {@link Pool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface VertxSqlPoolConfigurer {

	/**
	 * Configures the {@link Pool}.
	 * 
	 * @param context {@link VertxSqlPoolConfigurerContext}.
	 * @throws Exception If fails to configure.
	 */
	void configure(VertxSqlPoolConfigurerContext context) throws Exception;

}