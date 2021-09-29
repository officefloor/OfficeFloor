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
		super(null, null);
	}

	/**
	 * Instantiate with defaults.
	 * 
	 * @param testDatabase {@link CosmosTestDatabase}.
	 */
	public CosmosDbRule(CosmosTestDatabase testDatabase) {
		super(null, testDatabase);
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
	 * ================= AbstractCosmosDbJunit ==================
	 */

	@Override
	protected void skipTestFailure(String message, Throwable testFailure) {
		Assume.assumeNoException(message, testFailure);
	}

	/*
	 * ====================== TestRule ==========================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Start CosmosDb
				CosmosDbRule.this.startCosmosDb(true);
				try {

					// Run the test
					base.evaluate();

				} catch (Throwable ex) {
					CosmosDbRule.this.handleTestFailure(ex);

				} finally {
					// Stop CosmosDb
					CosmosDbRule.this.stopCosmosDb();
				}
			}
		};
	}

}
