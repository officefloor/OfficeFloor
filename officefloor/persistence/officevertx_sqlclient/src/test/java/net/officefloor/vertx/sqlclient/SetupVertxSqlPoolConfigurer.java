/*-
 * #%L
 * Vertx SQL Client
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
