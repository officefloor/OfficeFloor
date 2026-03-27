/*-
 * #%L
 * CosmosDB
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

/**
 * Tests the {@link CosmosAsyncDbSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncDbSupplierTest extends AbstractCosmosSupplierTestCase {

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosAsyncDbSupplierSource> getSupplierSourceClass() {
		return CosmosAsyncDbSupplierSource.class;
	}

	@Override
	protected boolean isAsynchronous() {
		return true;
	}

}
