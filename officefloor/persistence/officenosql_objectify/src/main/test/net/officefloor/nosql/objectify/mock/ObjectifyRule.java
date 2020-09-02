/*-
 * #%L
 * Objectify Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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
