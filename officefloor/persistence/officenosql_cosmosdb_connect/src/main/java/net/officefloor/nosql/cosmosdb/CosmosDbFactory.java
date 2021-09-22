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

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;

/**
 * Factory for {@link CosmosClient} or {@link CosmosAsyncClient}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosDbFactory {

	/**
	 * Creates the {@link CosmosClient}.
	 * 
	 * @return {@link CosmosClient}.
	 * @throws Exception If fails to create {@link CosmosClient}.
	 */
	CosmosClient createCosmosClient() throws Exception;

	/**
	 * Creates the {@link CosmosAsyncClient}.
	 * 
	 * @return {@link CosmosAsyncClient}.
	 * @throws Exception If fails to create {@link CosmosAsyncClient}.
	 */
	CosmosAsyncClient createCosmosAsyncClient() throws Exception;

}
