/*-
 * #%L
 * DynamoDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	public void doTest(AmazonDynamoDB dynamo) {

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