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

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.cloud.datastore.Datastore;
import com.googlecode.objectify.Objectify;

/**
 * {@link Extension} for running {@link Objectify} with local {@link Datastore}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyExtension extends AbstractObjectifyJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if around each test.
	 */
	private boolean isEach = true;

	/*
	 * ================= Extension =======================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		
		// Setup local data store
		this.setupLocalDataStore();
		
		// Around all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		
		// Setup local data store for each
		if (this.isEach) {
			this.setupLocalDataStore();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		
		// Tear down local data store if for each
		if (this.isEach) {
			this.tearDownLocalDataStore();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		
		// Tear down local data store if around all
		if (!this.isEach) {
			this.tearDownLocalDataStore();
		}
		
		// Reset
		this.isEach = true;
	}

}
