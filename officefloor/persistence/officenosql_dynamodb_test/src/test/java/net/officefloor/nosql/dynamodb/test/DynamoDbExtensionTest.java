/*-
 * #%L
 * DynamoDB Persistence Testing
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

package net.officefloor.nosql.dynamodb.test;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link DynamoDbExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbExtensionTest extends AbstractDynamoDbTestCase {

	public final @RegisterExtension @Order(1) DynamoDbExtension dynamo = new DynamoDbExtension();

	public final @RegisterExtension @Order(2) DynamoDbConnectExtension connect = new DynamoDbConnectExtension();

	@UsesDockerTest
	public void testExtension() throws Exception {
		this.doInteractTest(this.dynamo.getAmazonDynamoDb());
	}

	@UsesDockerTest
	public void testConnection() {
		this.doInteractTest(this.connect.getAmazonDynamoDb());
	}

}
