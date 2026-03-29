/*-
 * #%L
 * DynamoDB Persistence
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

package net.officefloor.nosql.dynamodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link DynamoDbSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoDbSupplierTest {

	@RegisterExtension
	public final DynamoDbExtension dynamoDb = new DynamoDbExtension();

	/**
	 * Validates the specification.
	 */
	@Test
	public void specification() {
		SupplierLoaderUtil.validateSpecification(DynamoDbSupplierSource.class);
	}

	/**
	 * Ensure {@link DynamoDBMapper} working. \
	 */
	@Test
	public void dynamoDbMapper() throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Register DynamoDbMapper
			context.getOfficeArchitect().addSupplier("DYNAMO_DB", DynamoDbSupplierSource.class.getName()).addProperty(
					DynamoDbSupplierSource.PROPERTY_ENTITY_LOCATORS, TestDynamoEntityLocator.class.getName());

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Invoke functionality
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", null);

			// Ensure table set up
			AmazonDynamoDB dynamo = this.dynamoDb.getAmazonDynamoDb();
			TableDescription table = dynamo.describeTable("TestEntity").getTable();
			assertEquals(1, table.getItemCount(), "Should have created entity");

			// Retrieve
			TestEntity retrieved = this.dynamoDb.getDynamoDbMapper().load(TestEntity.class, TestSection.entity.getId(),
					TestSection.entity.getAmount());
			assertEquals("TEST", retrieved.getMessage(), "Should retrieve stored entity");
		}
	}

	public static class TestSection {

		public static TestEntity entity;

		public void service(DynamoDBMapper mapper) {

			// Save
			entity = new TestEntity("TEST", 10, 1);
			mapper.save(entity);
		}
	}

}
