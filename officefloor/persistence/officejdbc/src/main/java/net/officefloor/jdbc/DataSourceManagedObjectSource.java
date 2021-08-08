/*-
 * #%L
 * JDBC Persistence
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
