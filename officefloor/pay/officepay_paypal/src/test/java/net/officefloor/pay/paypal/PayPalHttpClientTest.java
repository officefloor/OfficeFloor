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

package net.officefloor.pay.paypal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Money;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource.BasicCredentials;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServer.MockWoofInput;

/**
 * Tests the {@link PayPalHttpClientManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalHttpClientTest {

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(PayPalHttpClientManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	@Test
	public void type() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(PayPalHttpClient.class);
		type.addFunctionDependency(PayPalConfigurationRepository.class.getSimpleName(),
				PayPalConfigurationRepository.class, null);
		type.setInput(true);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, PayPalHttpClientManagedObjectSource.class);
	}

	/**
	 * Ensure can point to particular {@link PayPalEnvironment}.
	 */
	@Test
	public void configureEnvironment() throws Throwable {

		// Start mock server
		try (OfficeFloor mockPayPal = MockWoofServer.open(7171, MockPayPalService.class)) {

			// Configure service
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.office((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// Add PayPal (mocked)
				office.addOfficeManagedObjectSource("PAYPAL", PayPalHttpClientManagedObjectSource.class.getName())
						.addOfficeManagedObject("PAYPAL", ManagedObjectScope.THREAD);
				Singleton.load(office, new MockPayPalConfigurationRepository(new PayPalEnvironment("MOCK_CLIENT_ID",
						"MOCK_CLIENT_SECRET", "http://localhost:7171", "http://localhost:7171")));

				// Add section
				context.addSection("SECTION", ConfigureEnvironmentService.class);
			});
			try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

				// Trigger the servicing
				Closure<HttpResponse<Order>> closure = new Closure<>();
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", closure);

				// Ensure have order
				Order order = closure.value.result();
				assertNotNull(order, "Should have order");
				assertEquals("ORDER_ID", order.id(), "Incorrect order id");
				assertEquals("COMPLETED", order.status(), "Incorrect status");
			}
		}
	}

	public static class ConfigureEnvironmentService {
		public void service(@Parameter Closure<HttpResponse<Order>> closure, PayPalHttpClient client)
				throws IOException {

			// Order the request
			OrdersCreateRequest request = new OrdersCreateRequest().requestBody(new OrderRequest()
					.checkoutPaymentIntent("CAPTURE")
					.applicationContext(new ApplicationContext().brandName("OfficeFloor").landingPage("BILLING"))
					.purchaseUnits(Arrays
							.asList(new PurchaseUnitRequest().referenceId("MOCK_ID").description("Test create order")
									.amountWithBreakdown(new AmountWithBreakdown().currencyCode("AUD").value("5.00")
											.amountBreakdown(new AmountBreakdown()
													.itemTotal(new Money().currencyCode("AUD").value("5.00"))
													.taxTotal(new Money().currencyCode("AUD").value("0.50"))))
									.items(Arrays.asList(new Item().name("Domain").description("Domain subscription")
											.unitAmount(new Money().currencyCode("AUD").value("4.50"))
											.tax(new Money().currencyCode("AUD").value("0.50")).quantity("1"))))));
			HttpResponse<Order> orderResponse = client.execute(request);
			assertEquals(200, orderResponse.statusCode(), "Should be successful");
			closure.value = orderResponse;
		}
	}

	public static class MockPayPalService {

		@MockWoofInput(method = "POST", path = "/v1/oauth2/token")
		public void auth(ServerHttpConnection connection) throws IOException {

			// Ensure correct environment details
			BasicCredentials credentials = BasicHttpSecuritySource.getBasicCredentials(connection.getRequest());
			assertNotNull(credentials, "Should have credentials");
			assertEquals("MOCK_CLIENT_ID", credentials.userId, "Incorrect client id");
			assertEquals("MOCK_CLIENT_SECRET", credentials.password, "Incorrect client secret");

			// Provide response
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.getHeaders().addHeader(new HttpHeaderName("Content-Type", true), "application/json");
			response.getEntityWriter().write("{}");
		}

		@MockWoofInput(method = "POST", path = "/v2/checkout/orders")
		public void order(ServerHttpConnection connection) throws IOException {
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.getHeaders().addHeader(new HttpHeaderName("Content-Type", true), "application/json");
			Order order = new Order().id("ORDER_ID").status("COMPLETED");
			response.getEntityWriter().write(new Json().serialize(order));
		}
	}

	private static class MockPayPalConfigurationRepository implements PayPalConfigurationRepository {

		private final PayPalEnvironment environment;

		private MockPayPalConfigurationRepository(PayPalEnvironment environment) {
			this.environment = environment;
		}

		@Override
		public PayPalEnvironment createPayPalEnvironment() {
			return this.environment;
		}
	}

}
