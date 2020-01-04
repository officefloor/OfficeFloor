/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.datasource;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Tests the {@link DefaultDataSourceFactory} for the
 * {@link ConnectionPoolDataSourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultConnectionPoolDataSourceFactory extends AbstractDataSourceFactoryTestCase {

	@Override
	protected Class<? extends CommonDataSource> getDataSourceType() {
		return ConnectionPoolDataSource.class;
	}

	@Override
	protected CommonDataSource createCommonDataSource(SourceContext sourceContext) throws Exception {
		return new DefaultDataSourceFactory().createConnectionPoolDataSource(sourceContext);
	}

}
