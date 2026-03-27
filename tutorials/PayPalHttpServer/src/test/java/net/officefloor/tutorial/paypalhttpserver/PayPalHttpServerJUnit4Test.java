package net.officefloor.tutorial.paypalhttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;

import net.officefloor.pay.paypal.mock.PayPalRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CaptureOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CapturedOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CreateOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CreatedOrder;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the PayPal HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpServerJUnit4Test {

	// START SNIPPET: tutorial
	public final PayPalRule payPal = new PayPalRule();

	public final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.payPal).around(this.server);

	@Test
	public void createOrder() throws Exception {

		// Record create order
		this.payPal.addOrdersCreateResponse(new Order().id("MOCK_ORDER_ID").status("CREATED")).validate((request) -> {
			assertEquals("Incorrect order", "/v2/checkout/orders?", request.path());
			OrderRequest order = (OrderRequest) request.requestBody();
			assertEquals("Incorrect intent", "CAPTURE", order.checkoutPaymentIntent());
			assertEquals("Incorrect amount", "5.00", order.purchaseUnits().get(0).amountWithBreakdown().value());
		});

		// Create
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/create", new CreateOrder("AUD")));
		response.assertJson(200, new CreatedOrder("MOCK_ORDER_ID", "CREATED"));
	}

	@Test
	public void captureOrder() throws Exception {

		// Record capture order
		this.payPal.addOrdersCaptureResponse(new Order().id("MOCK_ORDER_ID").status("COMPLETED"))
				.validate((request) -> {
					assertEquals("Incorrect order", "/v2/checkout/orders/MOCK_ORDER_ID/capture?", request.path());
				});

		// Create
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/capture", new CaptureOrder("MOCK_ORDER_ID")));
		response.assertJson(200, new CapturedOrder("MOCK_ORDER_ID", "COMPLETED"));
	}
	// END SNIPPET: tutorial

}
