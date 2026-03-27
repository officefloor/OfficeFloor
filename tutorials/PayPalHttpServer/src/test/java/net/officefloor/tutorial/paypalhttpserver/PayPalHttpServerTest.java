package net.officefloor.tutorial.paypalhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.paypal.orders.OrderRequest;

import net.officefloor.pay.paypal.mock.PayPalExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CaptureOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CapturedOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CreateOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CreatedOrder;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the PayPal HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final PayPalExtension payPal = new PayPalExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void createOrder() throws Exception {

		// Record create order
		this.payPal.addOrdersCreateResponse(new com.paypal.orders.Order().id("MOCK_ORDER_ID").status("CREATED"))
				.validate((request) -> {
					assertEquals("/v2/checkout/orders?", request.path(), "Incorrect order");
					OrderRequest order = (OrderRequest) request.requestBody();
					assertEquals("CAPTURE", order.checkoutPaymentIntent(), "Incorrect intent");
					assertEquals("5.00", order.purchaseUnits().get(0).amountWithBreakdown().value(),
							"Incorrect amount");
				});

		// Create
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/create", new CreateOrder("AUD")));
		response.assertJson(200, new CreatedOrder("MOCK_ORDER_ID", "CREATED"));
	}

	@Test
	public void captureOrder() throws Exception {

		// Record capture order
		this.payPal.addOrdersCaptureResponse(new com.paypal.orders.Order().id("MOCK_ORDER_ID").status("COMPLETED"))
				.validate((request) -> {
					assertEquals("/v2/checkout/orders/MOCK_ORDER_ID/capture?", request.path(), "Incorrect order");
				});

		// Create
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/capture", new CaptureOrder("MOCK_ORDER_ID")));
		response.assertJson(200, new CapturedOrder("MOCK_ORDER_ID", "COMPLETED"));
	}
	// END SNIPPET: tutorial

}
