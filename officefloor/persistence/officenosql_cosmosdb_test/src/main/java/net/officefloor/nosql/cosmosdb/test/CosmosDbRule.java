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

package net.officefloor.nosql.cosmosdb.test;

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
	 * Instantiate with default {@link Configuration}.
	 */
	public CosmosDbRule() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public CosmosDbRule(Configuration configuration) {
		super(configuration);
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
				CosmosDbRule.this.startCosmosDb();
				try {

					// Run the test
					base.evaluate();

				} finally {
					// Stop CosmosDb
					CosmosDbRule.this.stopCosmosDb();
				}
			}
		};
	}

}
