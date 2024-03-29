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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import lombok.Data;

/**
 * Tests the JUnit {@link DynamoDB} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDynamoDbTestCase {

	/**
	 * Undertakes test.
	 * 
	 * @param dynamo {@link AmazonDynamoDB}.
	 */
	public void doInteractTest(AmazonDynamoDB dynamo) {

		// Create the mapper
		DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

		// Create table (as should be fresh instance each time)
		new DynamoDB(dynamo).createTable(mapper.generateCreateTableRequest(TestEntity.class)
				.withProvisionedThroughput(new ProvisionedThroughput(10L, 10L)));

		// Ensure list table
		TableDescription table = dynamo.describeTable("TestEntity").getTable();
		assertEquals("TestEntity", table.getTableName());
		assertEquals(1, table.getAttributeDefinitions().size(), "Incorrect numbr of attributes");
	}

	@Data
	@DynamoDBTable(tableName = "TestEntity")
	public static class TestEntity {

		@DynamoDBHashKey
		@DynamoDBAutoGeneratedKey
		private String id;

		private String message;
	}

}
