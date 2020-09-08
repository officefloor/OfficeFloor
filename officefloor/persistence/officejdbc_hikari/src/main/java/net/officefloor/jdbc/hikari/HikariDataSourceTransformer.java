/*-
 * #%L
 * Hikari JDBC Pooling
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
