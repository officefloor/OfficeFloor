/*-
 * #%L
 * PostgreSQL Persistence
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

package net.officefloor.jdbc.postgresql;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * PostgreSQL {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlReadOnlyConnectionManagedObjectSource extends ReadOnlyConnectionManagedObjectSource
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

}
