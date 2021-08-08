/*-
 * #%L
 * PayPal
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

package net.officefloor.pay.paypal.mock;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link PayPalRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpRuleTest extends AbstractPayPalHttpTestCase {

	/**
	 * {@link PayPalRule} to test.
	 */
	@Rule
	public final PayPalRule rule = new PayPalRule();

	/*
	 * ==================== AbstractPayPalHttpTestCase ===================
	 */

	@Override
	protected AbstractPayPalJUnit getPayPalJUnit() {
		return this.rule;
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
