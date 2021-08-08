/*-
 * #%L
 * Identity for Google Logins
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

package net.officefloor.identity.google.mock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

/**
 * {@link Extension} for mocking the {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIdTokenExtension extends AbstractGoogleIdTokenJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if around each test.
	 */
	private boolean isEach = true;

	/*
	 * ==================== Extension ======================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Setup
		this.setupMockTokens();

		// Flag for around all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Setup if for each
		if (this.isEach) {
			this.setupMockTokens();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Tear down if for each
		if (this.isEach) {
			this.tearDownMockTokens();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Tear down if after all
		if (!this.isEach) {
			this.tearDownMockTokens();
		}

		// Reset
		this.isEach = true;
	}

}
