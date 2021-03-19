/*-
 * #%L
 * PayPal
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

package net.officefloor.pay.paypal.mock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.paypal.core.PayPalHttpClient;

/**
 * {@link Extension} to mock {@link PayPalHttpClient} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalExtension extends AbstractPayPalJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if around each test.
	 */
	private boolean isEach = true;

	/*
	 * ======================= Extension ==========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Setup before all
		this.setupMockPayPaylHttpClient();

		// Set around all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Setup if for each
		if (this.isEach) {
			this.setupMockPayPaylHttpClient();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Tear down if for each
		if (this.isEach) {
			this.tearDownMockPayPaylHttpClient();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Tear down if after all
		if (!this.isEach) {
			this.tearDownMockPayPaylHttpClient();
		}

		// Reset
		this.isEach = true;
	}

}
