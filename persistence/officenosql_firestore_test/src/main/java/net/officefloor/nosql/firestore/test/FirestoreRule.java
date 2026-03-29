/*-
 * #%L
 * Firestore Persistence Testing
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

package net.officefloor.nosql.firestore.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.firestore.Firestore;

/**
 * {@link Firestore} {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreRule extends AbstractFirestoreJunit<FirestoreRule> implements TestRule {

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public FirestoreRule() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public FirestoreRule(Configuration configuration) {
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

				// Start Firestore
				FirestoreRule.this.startFirestore();
				try {

					// Run the test
					base.evaluate();

				} finally {
					// Stop Firestore
					FirestoreRule.this.stopFirestore();
				}
			}
		};
	}

}
