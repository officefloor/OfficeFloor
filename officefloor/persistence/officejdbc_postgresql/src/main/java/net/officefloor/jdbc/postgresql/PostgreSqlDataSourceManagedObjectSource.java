/*-
 * #%L
 * PostgreSQL Persistence
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

package net.officefloor.jdbc.postgresql;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * PostgreSQL {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlDataSourceManagedObjectSource extends DataSourceManagedObjectSource
		implements PostgreSqlDataSourceFactory {

	/*
	 * =============== ConnectionManagedObjectSource =================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		PostgreSqlDataSourceFactory.loadSpecification(context);
	}

	@Override
	protected DataSourceFactory getDataSourceFactory(SourceContext context) {
		return this;
	}

	@Override
	protected ConnectionPoolDataSourceFactory getConnectionPoolDataSourceFactory(SourceContext context) {
		return this;
	}

}
