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

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import net.officefloor.test.JUnit5Skip;

/**
 * {@link Extension} for CosmosDb.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbExtension extends AbstractCosmosDbJunit<CosmosDbExtension>
		implements BeforeEachCallback, TestExecutionExceptionHandler, AfterEachCallback {

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

	/**
	 * Handles possible skip.
	 * 
	 * @param context {@link ExtensionContext}.
	 */
	private void handlePossibleSkip(ExtensionContext context) {
		Optional<Throwable> failure = context.getExecutionException();
		if (failure.isPresent() && this.isSkipFailure()) {
			JUnit5Skip.skip(context, SKIP_MESSAGE, failure.get());
		}
	}

	/*
	 * ================== Extension ===================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// New database for each test
		this.startCosmosDb();
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		this.handleTestFailure(throwable, (message, cause) -> JUnit5Skip.skip(context, message, cause));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		try {

			// Stop if for each
			this.stopCosmosDb();

		} finally {
			this.handlePossibleSkip(context);
		}
	}

}
