/*-
 * #%L
 * JWT Security
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
