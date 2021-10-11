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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Abstract functionality for testing Cosmos DB {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCosmosSupplierTestCase {

	/**
	 * {@link CosmosDbExtension} to provide Cosmos DB emulator.
	 */
	public @RegisterExtension final CosmosDbExtension cosmosDb = new CosmosDbExtension();

	/**
	 * Obtains the {@link SupplierSource} {@link Class} to test.
	 * 
	 * @param <S> Type of {@link SupplierSource} {@link Class}.
	 * @return {@link SupplierSource} {@link Class} to test.
	 */
	protected abstract <S extends SupplierSource> Class<S> getSupplierSourceClass();

	/**
	 * Indicates if {@link CosmosAsyncDatabase}.
	 * 
	 * @return <code>true</code> if asynchronous.
	 */
	protected abstract boolean isAsynchronous();

	/**
	 * Validates the specification.
	 */
	@UsesDockerTest
	public void specification() {
		SupplierLoaderUtil.validateSpecification(this.getSupplierSourceClass());
	}

	/**
	 * Ensure {@link CosmosDatabase} working.
	 */
	@UsesDockerTest
	public void cosmosDb() throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Register Cosmos
			context.getOfficeArchitect().addSupplier("COSMOS_DB", this.getSupplierSourceClass().getName()).addProperty(
					AbstractCosmosDbSupplierSource.PROPERTY_ENTITY_LOCATORS, TestCosmosEntityLocator.class.getName());

			// Register test logic
			context.addSection("TEST", this.isAsynchronous() ? TestAsyncSection.class : TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Clear state
			TestSection.reset();
			TestAsyncSection.reset();

			// Invoke functionality
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", null);

			// Undertake validation
			if (this.isAsynchronous()) {
				TestAsyncSection.validate();
			} else {
				TestSection.validate();
			}
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
			CosmosDbUtil.retry(() -> entities.getContainer(TestDefaultEntity.class).createItem(defaultEntity));

			// Save annotated entity
			annotatedEntity = new TestAnnotatedEntity(UUID.randomUUID().toString(), "Test Annotated Entity");
			CosmosDbUtil.retry(() -> entities.getContainer(TestAnnotatedEntity.class).createItem(annotatedEntity));
		}

		public static void reset() {
			cosmosEntities = null;
			defaultEntity = null;
			annotatedEntity = null;
		}

		public static void validate() {

			// Retrieve default
			TestEntity retrievedDefault = CosmosDbUtil
					.retry(() -> cosmosEntities.getContainer(TestDefaultEntity.class).readItem(defaultEntity.getId(),
							cosmosEntities.createPartitionKey(defaultEntity), TestDefaultEntity.class))
					.getItem();
			assertEquals("Test Default Entity", retrievedDefault.getMessage(), "Should retrieve stored default entity");

			// Retrieve annotated
			TestEntity retrievedAnnotated = CosmosDbUtil.retry(
					() -> cosmosEntities.getContainer(TestAnnotatedEntity.class).readItem(annotatedEntity.getId(),
							cosmosEntities.createPartitionKey(annotatedEntity), TestAnnotatedEntity.class))
					.getItem();
			assertEquals("Test Annotated Entity", retrievedAnnotated.getMessage(),
					"Should retrieve stored annotated entity");
		}
	}

	public static class TestAsyncSection {

		public static CosmosAsyncEntities cosmosEntities;

		public static TestDefaultEntity defaultEntity;

		public static TestAnnotatedEntity annotatedEntity;

		public void service(CosmosAsyncEntities entities, AsynchronousFlow asyncDefault,
				AsynchronousFlow asyncAnnotated) {
			cosmosEntities = entities;

			// Save default and annotated entity
			defaultEntity = new TestDefaultEntity(UUID.randomUUID().toString(), "Test Default Entity");
			annotatedEntity = new TestAnnotatedEntity(UUID.randomUUID().toString(), "Test Annotated Entity");
			CosmosAsyncContainer container = entities.getContainer(TestDefaultEntity.class);

			// Create the items
			createItem(defaultEntity, container, asyncDefault);
			createItem(annotatedEntity, container, asyncAnnotated);
		}

		private static void createItem(Object entity, CosmosAsyncContainer container, AsynchronousFlow async) {
			container.createItem(entity).subscribe((result) -> {
				async.complete(null);
			}, (error) -> async.complete(() -> {
				throw error;
			}));
		}

		public static void reset() {
			cosmosEntities = null;
			defaultEntity = null;
			annotatedEntity = null;
		}

		public static void validate() {

			// Retrieve default
			TestEntity retrievedDefault = cosmosEntities
					.getContainer(TestDefaultEntity.class).readItem(defaultEntity.getId(),
							cosmosEntities.createPartitionKey(defaultEntity), TestDefaultEntity.class)
					.retryWhen(CosmosDbUtil.retry()).block().getItem();
			assertEquals("Test Default Entity", retrievedDefault.getMessage(), "Should retrieve stored default entity");

			// Retrieve annotated
			TestEntity retrievedAnnotated = cosmosEntities
					.getContainer(TestAnnotatedEntity.class).readItem(annotatedEntity.getId(),
							cosmosEntities.createPartitionKey(annotatedEntity), TestAnnotatedEntity.class)
					.retryWhen(CosmosDbUtil.retry()).block().getItem();
			assertEquals("Test Annotated Entity", retrievedAnnotated.getMessage(),
					"Should retrieve stored annotated entity");
		}
	}

}
