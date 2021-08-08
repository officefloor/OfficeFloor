/*-
 * #%L
 * Objectify Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.nosql.objectify.mock;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.datastore.Datastore;
import com.googlecode.objectify.Objectify;

/**
 * {@link Rule} for running {@link Objectify} with local {@link Datastore}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyRule extends AbstractObjectifyJUnit implements TestRule {

	/*
	 * ================= TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {

		// Return statement to start application
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try {
					// Setup local data store
					ObjectifyRule.this.setupLocalDataStore();

					// Undertake test
					base.evaluate();

				} finally {
					// Tear down loca data store
					ObjectifyRule.this.tearDownLocalDataStore();
				}
			}
		};
	}

}
