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
