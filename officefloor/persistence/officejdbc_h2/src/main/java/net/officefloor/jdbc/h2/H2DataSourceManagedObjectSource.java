/*-
 * #%L
 * H2 Persistence
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

package net.officefloor.jdbc.h2;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * H2 {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2DataSourceManagedObjectSource extends DataSourceManagedObjectSource implements H2DataSourceFactory {

	/*
	 * ============= ConnectionManagedObjectSource ===========
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		H2DataSourceFactory.loadSpecification(context);
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
