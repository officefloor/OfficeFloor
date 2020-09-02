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