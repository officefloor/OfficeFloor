/*-
 * #%L
 * Vertx SQL Client
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
