/*-
 * #%L
 * Firestore Persistence Testing
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

package net.officefloor.nosql.firestore.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.cloud.firestore.Firestore;

/**
 * {@link Extension} for {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreExtension extends AbstractFirestoreJunit<FirestoreExtension>
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if run DynamoDb for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public FirestoreExtension() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public FirestoreExtension(Configuration configuration) {
		super(configuration);
	}

	/*
	 * ================== Extension ===================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start
		this.startFirestore();

		// Shutdown after all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Determine if start for each
		if (this.isEach) {
			this.startFirestore();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop if for each
		if (this.isEach) {
			this.stopFirestore();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop if after all
		if (!this.isEach) {
			this.stopFirestore();
		}
	}

}
