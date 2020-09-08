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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests the {@link PayPalRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpExtensionTest extends AbstractPayPalHttpTestCase {

	/**
	 * {@link PayPalExtension} to test.
	 */
	@RegisterExtension
	public final PayPalExtension extension = new PayPalExtension();

	/*
	 * ==================== AbstractPayPalHttpTestCase ===================
	 */

	@Override
	protected AbstractPayPalJUnit getPayPalJUnit() {
		return this.extension;
	}

	@Test
	@Override
	public void mockCreateOrder() throws Throwable {
		super.mockCreateOrder();
	}

	@Test
	@Override
	public void mockCaptureOrder() throws Throwable {
		super.mockCaptureOrder();
	}

	@Test
	@Override
	public void unexpectedInteraction() throws Throwable {
		super.unexpectedInteraction();
	}

	@Test
	@Override
	public void missingInteraction() throws Throwable {
		super.missingInteraction();
	}

	@Test
	@Override
	public void validateHttpRequest() throws Throwable {
		super.validateHttpRequest();
	}

	@Test
	@Override
	public void payPalError() throws Throwable {
		super.payPalError();
	}

}
