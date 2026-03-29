/*-
 * #%L
 * CosmosDB Connect
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClientBuilder;

/**
 * Decorates the {@link CosmosClientBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosClientBuilderDecorator {

	/**
	 * Decorates the {@link CosmosClientBuilder}.
	 * 
	 * @param builder {@link CosmosClientBuilder} to decorate.
	 * @return Decorated {@link CosmosClientBuilder}.
	 * @throws Exception If fails to decorate the {@link CosmosClientBuilder}.
	 */
	CosmosClientBuilder decorate(CosmosClientBuilder builder) throws Exception;

}
