/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.mock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.web.jwt.JwtHttpSecuritySource;

/**
 * <p>
 * {@link Extension} to mock JWT access tokens for the
 * {@link JwtHttpSecuritySource}.
 * <p>
 * This allows generating access tokens for testing the application.
 * 
 * @author Daniel Sagenschneider
 */
public class MockJwtAccessTokenExtension extends AbstractMockJwtAccessTokenJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if override for each test.
	 */
	private boolean isEach = true;

	/*
	 * =================== Extension =====================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Setup override
		this.setupMockKeys();

		// Override all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Setup override if for each
		if (this.isEach) {
			this.setupMockKeys();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Tear down override if for each
		if (this.isEach) {
			this.teardownMockKeys();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Tear down override if all tests
		if (!this.isEach) {
			this.teardownMockKeys();
		}

		// Reset for each
		this.isEach = true;
	}
}
