/*-
 * #%L
 * Hikari JDBC Pooling
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

package net.officefloor.jdbc.hikari;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.jdbc.datasource.DataSourceTransformer;
import net.officefloor.jdbc.datasource.DataSourceTransformerContext;
import net.officefloor.jdbc.datasource.DataSourceTransformerServiceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Wraps the {@link DataSource} with {@link HikariDataSource} for pooling.
 * 
 * @author Daniel Sagenschneider
 */
public class HikariDataSourceTransformer implements DataSourceTransformer, DataSourceTransformerServiceFactory {

	/*
	 * ================== DataSourceTransformerServiceFactory =============
	 */

	@Override
	public DataSourceTransformer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== DataSourceTransformer ======================
	 */

	@Override
	public DataSource transformDataSource(DataSourceTransformerContext context) throws Exception {

		// Create the Hikari DataSource
		HikariDataSource hikari = new HikariDataSource();
		hikari.setDataSource(context.getDataSource());

		// Load properties to configure pooling
		DefaultDataSourceFactory.loadProperties(hikari, context.getSourceContext());

		// Return the Hikari
		return hikari;
	}

}
