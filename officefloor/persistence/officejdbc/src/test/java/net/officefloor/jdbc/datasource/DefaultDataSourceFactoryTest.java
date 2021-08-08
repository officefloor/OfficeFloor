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

package net.officefloor.jdbc.datasource;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Tests the {@link DefaultDataSourceFactory} for {@link DataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultDataSourceFactoryTest extends AbstractDataSourceFactoryTestCase {

	@Override
	protected Class<? extends CommonDataSource> getDataSourceType() {
		return DataSource.class;
	}

	@Override
	protected CommonDataSource createCommonDataSource(SourceContext sourceContext) throws Exception {
		return new DefaultDataSourceFactory().createDataSource(sourceContext);
	}

}
