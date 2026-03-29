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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests using {@link DynamoDB}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoDbTest {

	@RegisterExtension
	public final DynamoDbExtension dynamoDb = new DynamoDbExtension();

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(DynamoDbMapperManagedObjectSource.class);
	}

	/**
	 * Ensure correct meta-data.
	 */
	@Test
	public void metaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(DynamoDBMapper.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, DynamoDbMapperManagedObjectSource.class);
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
			context.getOfficeArchitect()
					.addOfficeManagedObjectSource("DYNAMO_DB", DynamoDbMapperManagedObjectSource.class.getName())
					.addOfficeManagedObject("DYNAMO_DB", ManagedObjectScope.THREAD);

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

			// Ensure attributes
			assertNotNull(entity.getId(), "Should generate identifier");
			assertNotNull(entity.getLastModified(), "Should have modified date");
			assertEquals("TEST", entity.getMessage());

			// Format date
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			// Ensure can query by secondary index
			Map<String, AttributeValue> queryValues = new HashMap<>();
			queryValues.put(":v1", new AttributeValue().withN(String.valueOf(entity.getUserId())));
			queryValues.put(":v2", new AttributeValue().withS(dateFormatter.format(entity.getLastModified())));
			List<TestEntity> queryResults = mapper.query(TestEntity.class,
					new DynamoDBQueryExpression<TestEntity>().withIndexName("userId-lastModified-index")
							.withKeyConditionExpression("userId = :v1 and lastModified >= :v2")
							.withExpressionAttributeValues(queryValues).withConsistentRead(false));
			assertEquals(entity.getId(), queryResults.get(0).getId(), "Incorrect queried entity");

			// Ensure able to obtain entity
			TestEntity retrieved = mapper.load(TestEntity.class, entity.getId(), entity.getAmount());
			assertEquals("TEST", retrieved.getMessage(), "Should obtain all attributes");
		}
	}

}
