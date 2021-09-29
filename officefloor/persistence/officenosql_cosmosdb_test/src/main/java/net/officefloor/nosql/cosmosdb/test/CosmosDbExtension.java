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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

/**
 * {@link Extension} for CosmosDb.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbExtension extends AbstractCosmosDbJunit<CosmosDbExtension> implements BeforeAllCallback,
		BeforeEachCallback, TestExecutionExceptionHandler, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if run DynamoDb for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate with defaults.
	 */
	public CosmosDbExtension() {
		super(null, null);
	}

	/**
	 * Instantiate with specified {@link CosmosTestDatabase}.
	 * 
	 * @param testDatabase {@link CosmosTestDatabase}.
	 */
	public CosmosDbExtension(CosmosTestDatabase testDatabase) {
		super(null, testDatabase);
	}

	/**
	 * Instantiate.
	 * 
	 * @param emulatorInstance {@link CosmosEmulatorInstance}.
	 */
	public CosmosDbExtension(CosmosEmulatorInstance emulatorInstance) {
		super(emulatorInstance, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param emulatorInstance {@link CosmosEmulatorInstance}.
	 * @param testDatabase     {@link CosmosTestDatabase}.
	 */
	public CosmosDbExtension(CosmosEmulatorInstance emulatorInstance, CosmosTestDatabase testDatabase) {
		super(emulatorInstance, testDatabase);
	}

	/*
	 * ==================== AbstractCosmosDbJunit ====================
	 */

	@Override
	protected void skipTestFailure(String message, Throwable testFailure) {

		// Obtain stack trace
		String stackTrace = null;
		if (testFailure != null) {
			StringWriter buffer = new StringWriter();
			PrintWriter writer = new PrintWriter(buffer);
			testFailure.printStackTrace(writer);
			writer.flush();
			stackTrace = buffer.toString();
		}

		// Skip test
		Assumptions.assumeTrue(false, message + (stackTrace != null ? "\n\n" + stackTrace : ""));
	}

	/*
	 * ================== Extension ===================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start
		this.startCosmosDb(true);

		// Shutdown after all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Determine if start for each
		if (this.isEach) {
			this.startCosmosDb(true);
		}
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		this.handleTestFailure(throwable);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop if for each
		if (this.isEach) {
			this.stopCosmosDb();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop if after all
		if (!this.isEach) {
			this.stopCosmosDb();
		}
	}

}
