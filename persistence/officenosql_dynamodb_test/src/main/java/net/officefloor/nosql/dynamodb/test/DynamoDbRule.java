/*-
 * #%L
 * DynamoDB Persistence Testing
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

package net.officefloor.nosql.dynamodb.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 * {@link AmazonDynamoDB} {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbRule extends AbstractDynamoDbJunit implements TestRule {

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public DynamoDbRule() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public DynamoDbRule(Configuration configuration) {
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

				// Start DynamoDb
				DynamoDbRule.this.startAmazonDynamoDb();
				try {

					// Run the test
					base.evaluate();

				} finally {
					// Stop DynamoDb
					DynamoDbRule.this.stopAmazonDynamoDb();
				}
			}
		};
	}

}
