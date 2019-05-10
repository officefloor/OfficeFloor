/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.paypalhttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.orders.Order;

import net.officefloor.pay.paypal.mock.PayPalRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CaptureOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CreateOrder;
import net.officefloor.tutorial.paypalhttpserver.PayPalLogic.CreatedOrder;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the PayPal HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpServerTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	public final PayPalRule payPal = new PayPalRule();

	public final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.payPal).around(this.server);

	@Test
	public void createOrder() throws Exception {

		// Record create order
		this.payPal.addOrdersCreateResponse(new Order().id("MOCK_ORDER_ID").status("CREATED"));

		// Create
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/create").method(HttpMethod.POST)
				.header("Content-Type", "application/json").entity(mapper.writeValueAsString(new CreateOrder("AUD"))));
		String entity = response.getEntity(null);
		assertEquals("Should be successful: " + entity, 200, response.getStatus().getStatusCode());
		CreatedOrder order = mapper.readValue(entity, CreatedOrder.class);
		assertEquals("Incorrect order", "MOCK_ORDER_ID", order.getOrderId());
		assertEquals("Incorrect status", "CREATED", order.getStatus());
	}

	@Test
	public void captureOrder() throws Exception {

		// Record capture order
		this.payPal.addOrdersCaptureResponse(new Order().id("MOCK_ORDER_ID").status("COMPLETED"));

		// Create
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/capture").method(HttpMethod.POST)
				.header("Content-Type", "application/json")
				.entity(mapper.writeValueAsString(new CaptureOrder("MOCK_ORDER_ID"))));
		String entity = response.getEntity(null);
		assertEquals("Should be successful: " + entity, 200, response.getStatus().getStatusCode());
		CreatedOrder order = mapper.readValue(entity, CreatedOrder.class);
		assertEquals("Incorrect order", "MOCK_ORDER_ID", order.getOrderId());
		assertEquals("Incorrect status", "COMPLETED", order.getStatus());
	}

}