/*-
 * #%L
 * CosmosDB Persistence Testing
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

package net.officefloor.nosql.cosmosdb.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link CosmosDbExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbExtensionTest extends AbstractCosmosDbTestCase {

	public static final @RegisterExtension CosmosDbExtension cosmos = new CosmosDbExtension();

	@UsesDockerTest
	public void synchronous() {
		this.doSynchronousTest(cosmos.getCosmosDatabase());
	}

	@UsesDockerTest
	public void asynchronous() {
		this.doAsynchronousTest(cosmos.getCosmosAsyncDatabase());
	}

	@UsesDockerTest
	public void cleanDatabase() {
		this.doCleanDatabaseTest(cosmos.getCosmosDatabase());
	}

	@UsesDockerTest
	public void cleanDatabaseRepeat() {
		this.doCleanDatabaseTest(cosmos.getCosmosDatabase());
	}

}
