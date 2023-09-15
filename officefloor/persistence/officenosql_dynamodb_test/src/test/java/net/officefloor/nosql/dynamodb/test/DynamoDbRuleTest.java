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

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link DynamoDbRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbRuleTest extends AbstractDynamoDbTestCase {

	public final @Rule(order = 1) DynamoDbRule dynamo = new DynamoDbRule();

	public final @Rule(order = 2) DynamoDbConnectRule connect = new DynamoDbConnectRule();

	@Test
	public void testRule() throws Exception {
		SkipJUnit4.skipDocker();
		this.doInteractTest(this.dynamo.getAmazonDynamoDb());
	}

	@Test
	public void testConnection() throws Exception {
		SkipJUnit4.skipDocker();
		this.doInteractTest(this.connect.getAmazonDynamoDb());
	}

}
