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

import java.util.function.BiFunction;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * CosmosDb {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbRule extends AbstractCosmosDbJunit<CosmosDbRule> implements TestRule {

	/**
	 * Instantiate with defaults.
	 */
	public CosmosDbRule() {
		super(null);
	}

	/**
	 * Instantiate with defaults.
	 * 
	 * @param testDatabase {@link CosmosTestDatabase}.
	 */
	public CosmosDbRule(CosmosTestDatabase testDatabase) {
		super(testDatabase);
	}

	/**
	 * Instantiate.
	 * 
	 * @param emulatorInstance {@link CosmosEmulatorInstance}.
	 */
	public CosmosDbRule(CosmosEmulatorInstance emulatorInstance) {
		super(emulatorInstance, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param emulatorInstance {@link CosmosEmulatorInstance}.
	 * @param testDatabase     {@link CosmosTestDatabase}.
	 */
	public CosmosDbRule(CosmosEmulatorInstance emulatorInstance, CosmosTestDatabase testDatabase) {
		super(emulatorInstance, testDatabase);
	}

	/*
	 * =================== FailureFactory =======================
	 */

	@Override
	public Throwable create(String message, Throwable cause) {
		Assume.assumeNoException(message, cause);
		return null; // already thrown
	}

	/*
	 * ====================== TestRule ==========================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Create the skip function for the exception
				BiFunction<String, Throwable, Throwable> skip = (message, cause) -> {
					try {
						Assume.assumeNoException(message, cause);
					} catch (Throwable skipEx) {
						return skipEx; // skip
					}
					return null; // not skip
				};

				// Start CosmosDb
				CosmosDbRule.this.startCosmosDb();
				try {

					// Run the test
					base.evaluate();

				} catch (Throwable ex) {
					CosmosDbRule.this.handleTestFailure(ex, skip);

				} finally {
					// Stop CosmosDb
					CosmosDbRule.this.stopCosmosDb(skip);
				}
			}
		};
	}

}
