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