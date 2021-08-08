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

import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link DataSourceTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DataSourceTransformerContext {

	/**
	 * Obtains the {@link DataSource} to transform.
	 * 
	 * @return {@link DataSource} to transform.
	 */
	DataSource getDataSource();

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
