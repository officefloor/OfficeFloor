/*-
 * #%L
 * DynamoDB Persistence
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

package net.officefloor.nosql.cosmosdb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link CosmosDbSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosDbSupplierTest {

	@RegisterExtension
	public final CosmosDbExtension cosmosDb = new CosmosDbExtension();

	/**
	 * Validates the specification.
	 */
	@Test
	public void specification() {
		SupplierLoaderUtil.validateSpecification(CosmosDbSupplierSource.class);
	}

	/**
	 * Ensure {@link CosmosDatabase} working.
	 */
	@Test
	public void cosmosDb() throws Throwable {

		// Ensure the cosmos running (before opening OfficeFloor)
		this.cosmosDb.getCosmosClient();

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Register Cosmos
			context.getOfficeArchitect().addSupplier("COSMOS_DB", CosmosDbSupplierSource.class.getName()).addProperty(
					CosmosDbSupplierSource.PROPERTY_ENTITY_LOCATORS, TestCosmosEntityLocator.class.getName());

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Clear state
			TestSection.cosmosEntities = null;
			TestSection.defaultEntity = null;
			TestSection.annotatedEntity = null;

			// Invoke functionality
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", null);

			// Retrieve default
			TestEntity retrievedDefault = TestSection.cosmosEntities.getContainer(TestDefaultEntity.class)
					.readItem(TestSection.defaultEntity.getId(),
							TestSection.cosmosEntities.createPartitionKey(TestSection.defaultEntity),
							TestDefaultEntity.class)
					.getItem();
			assertEquals("Test Default Entity", retrievedDefault.getMessage(), "Should retrieve stored default entity");

			// Retrieve annotated
			TestEntity retrievedAnnotated = TestSection.cosmosEntities.getContainer(TestAnnotatedEntity.class)
					.readItem(TestSection.annotatedEntity.getId(),
							TestSection.cosmosEntities.createPartitionKey(TestSection.annotatedEntity),
							TestAnnotatedEntity.class)
					.getItem();
			assertEquals("Test Annotated Entity", retrievedAnnotated.getMessage(),
					"Should retrieve stored annotated entity");
		}
	}

	public static class TestSection {

		public static CosmosEntities cosmosEntities;

		public static TestDefaultEntity defaultEntity;

		public static TestAnnotatedEntity annotatedEntity;

		public void service(CosmosEntities entities) {
			cosmosEntities = entities;

			// Save default entity
			defaultEntity = new TestDefaultEntity(UUID.randomUUID().toString(), "Test Default Entity");
			entities.getContainer(TestDefaultEntity.class).createItem(defaultEntity);

			// Save annotated entity
			annotatedEntity = new TestAnnotatedEntity(UUID.randomUUID().toString(), "Test Annotated Entity");
			entities.getContainer(TestAnnotatedEntity.class).createItem(annotatedEntity);
		}
	}

}
