package net.officefloor.vertx.sqlclient;

import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * Context for the {@link VertxSqlPoolConfigurer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface VertxSqlPoolConfigurerContext {

	/**
	 * Obtains the {@link SqlConnectOptions}.
	 * 
	 * @return {@link SqlConnectOptions}.
	 */
	SqlConnectOptions getSqlConnectOptions();

	/**
	 * Obtains the {@link PoolOptions}.
	 * 
	 * @return {@link PoolOptions}.
	 */
	PoolOptions getPoolOptions();

}