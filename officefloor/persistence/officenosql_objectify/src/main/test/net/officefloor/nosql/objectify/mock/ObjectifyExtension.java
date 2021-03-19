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
