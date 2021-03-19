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

package net.officefloor.jdbc;

import java.util.logging.Logger;

import javax.sql.DataSource;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;

/**
 * {@link ManagedObjectSource} for a {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedObjectSource extends AbstractJdbcManagedObjectSource implements ManagedObject {

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/*
	 * ============== AbstractConnectionManagedObjectSource =================
	 */

	@Override
	protected void setupMetaData(MetaDataContext<None, None> context) throws Exception {

		// Load the type
		context.setObjectClass(DataSource.class);
	}

	@Override
	protected void setupActive(ManagedObjectSourceContext<None> mosContext) throws Exception {

		// Capture the logger
		this.logger = mosContext.getLogger();

		// Obtain the data source
		this.dataSource = this.newDataSource(mosContext);

		// Validate connectivity
		this.setConnectivity(() -> new ConnectionConnectivity(this.dataSource.getConnection()));
	}

	@Override
	public void stop() {

		// Close the DataSource
		this.closeDataSource(this.dataSource, this.logger);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================== ManagedObject ================================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.dataSource;
	}

}
