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

import javax.sql.DataSource;

/**
 * Transforms the {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DataSourceTransformer {

	/**
	 * Transforms the {@link DataSource}.
	 * 
	 * @param context {@link DataSourceTransformerContext}.
	 * @return Transformed {@link DataSource}.
	 * @throws Exception If fails to transform the {@link DataSource}.
	 */
	DataSource transformDataSource(DataSourceTransformerContext context) throws Exception;

}
