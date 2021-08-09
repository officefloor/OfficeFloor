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

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Money;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.pay.paypal.PayPalConfigurationRepository;
import net.officefloor.pay.paypal.PayPalHttpClientManagedObjectSource;
import net.officefloor.pay.paypal.mock.AbstractPayPalJUnit.Interaction;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Tests the {@link PayPalRule}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPayPalHttpTestCase {

	private static final Json JSON = new Json();

	/**
	 * Obtains the {@link AbstractPayPalJUnit}.
	 * 
	 * @return {@link AbstractPayPalJUnit}.
	 */
	protected abstract AbstractPayPalJUnit getPayPalJUnit();

	/**
	 * Ensure can mock creating an {@link Order}.
	 */
	public void mockCreateOrder() throws Throwable {
		HttpResponse<Order> response = this.doOrder(MockCreateOrderService.class,
				(mock) -> mock.addOrdersCreateResponse(new Order().id("ORDER_ID").status("CREATED"), "TEST", "VALUE"));
		JUnitAgnosticAssert.assertEquals("VALUE", response.headers().header("TEST"), "Incorrect header");
		JUnitAgnosticAssert.assertEquals("ORDER_ID", response.result().id(), "Incorrect order Id");
		JUnitAgnosticAssert.assertEquals("CREATED", response.result().status(), "Incorrect status");
	}

	public static class MockCreateOrderService {
		public static void service(@Parameter Closure<HttpResponse<Order>> closure, PayPalHttpClient client)
				throws IOException {
			closure.value = createOrder(client);
		}

		public static OrdersCreateRequest createOrdersRequest() {
			return new OrdersCreateRequest().requestBody(new OrderRequest().checkoutPaymentIntent("CAPTURE")
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
		}

		public static HttpResponse<Order> createOrder(PayPalHttpClient client) throws IOException {
			OrdersCreateRequest request = createOrdersRequest();
			HttpResponse<Order> orderResponse = client.execute(request);
			JUnitAgnosticAssert.assertEquals(200, orderResponse.statusCode(), "Should be successful");
			return orderResponse;
		}
	}

	/**
	 * Ensure can mock capture {@link Order}.
	 */
	public void mockCaptureOrder() throws Throwable {
		Closure<String> orderId = new Closure<>();
		HttpResponse<Order> response = this.doOrder(MockCaptureService.class, (mock) -> mock
				.addOrdersCaptureResponse(new Order().id("ORDER_ID").status("COMPLETED")).validate((request) -> {
					orderId.value = mock.getOrderId(request);
				}));
		JUnitAgnosticAssert.assertEquals("ORDER_ID", response.result().id(), "Incorrect order Id");
		JUnitAgnosticAssert.assertEquals("COMPLETED", response.result().status(), "Incorrect status");
		JUnitAgnosticAssert.assertEquals("ORDER_ID", orderId.value, "Incorrect request order Id");
	}

	public static class MockCaptureService {
		public void service(@Parameter Closure<HttpResponse<Order>> closure, PayPalHttpClient client)
				throws IOException {
			OrdersCaptureRequest request = new OrdersCaptureRequest("ORDER_ID");
			request.requestBody(new OrderRequest());
			HttpResponse<Order> orderResponse = client.execute(request);
			JUnitAgnosticAssert.assertEquals(200, orderResponse.statusCode(), "Should be successful");
			closure.value = orderResponse;
		}
	}

	/**
	 * Ensure report unexpected {@link Interaction}.
	 */
	public void unexpectedInteraction() throws Throwable {
		try {
			this.doOrder(MockCreateOrderService.class, (rule) -> {
				// No interaction, so is unexpected
			});
			JUnitAgnosticAssert.fail("Should not be successful");
		} catch (AssertionError ex) {
			JUnitAgnosticAssert.assertEquals(
					"No PayPal interaction for OrdersCreateRequest("
							+ JSON.serialize(MockCreateOrderService.createOrdersRequest().requestBody()) + ")",
					ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure all {@link Interaction} instances occur.
	 */
	public void missingInteraction() throws Throwable {
		Order unexpected = new Order().status("MISSING");
		try {
			this.doOrder(MockCreateOrderService.class, (mock) -> {
				mock.addOrdersCreateResponse(new Order().status("USED"));

				// Record missing interaction
				mock.addOrdersCaptureResponse(unexpected);
			});

			// Validate interactions
			this.getPayPalJUnit().assertInteractions();
			JUnitAgnosticAssert.fail("Should not be successful");

		} catch (AssertionError ex) {
			JUnitAgnosticAssert.assertEquals("Not all PayPal interactions exercised\n\nRESPONSE: "
					+ JSON.serialize(unexpected) + ": Expected <0> but was <1>", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure can validate the {@link HttpRequest}.
	 */
	public void validateHttpRequest() throws Throwable {
		final Exception failure = new Exception("TEST");
		try {
			this.doOrder(MockCreateOrderService.class,
					(mock) -> mock.addOrdersCreateResponse(new Order()).validate((request) -> {
						OrderRequest orderRequest = (OrderRequest) request.requestBody();
						JUnitAgnosticAssert.assertEquals("CAPTURE", orderRequest.checkoutPaymentIntent(),
								"Incorrect intent");
						throw failure;
					}));

			// Validate interactions
			this.getPayPalJUnit().assertInteractions();
			JUnitAgnosticAssert.fail("Should not be successful");

		} catch (Exception ex) {
			JUnitAgnosticAssert.assertSame(failure, ex, "Should throw failure");
		}
	}

	/**
	 * Ensure can mock PayPal error.
	 */
	public void payPalError() throws Throwable {
		final IOException failure = new IOException("TEST");
		Closure<Exception> closure = new Closure<>();
		this.doOrder(MockErrorService.class, closure, (mock) -> mock.addException(failure));
		JUnitAgnosticAssert.assertSame(failure, closure.value, "Should throw failure");
	}

	public static class MockErrorService {
		public void service(@Parameter Closure<Exception> closure, PayPalHttpClient client) throws IOException {
			OrdersCaptureRequest request = new OrdersCaptureRequest("ORDER_ID");
			request.requestBody(new OrderRequest());
			try {
				client.execute(request);
				JUnitAgnosticAssert.fail("Should not be successful");
			} catch (Exception ex) {
				closure.value = ex;
			}
		}
	}

	/**
	 * Undertakes interacting with {@link Order}.
	 * 
	 * @param sectionSourceClass {@link SectionSource} {@link Class}.
	 * @return {@link HttpResponse}.
	 */
	private HttpResponse<Order> doOrder(Class<?> sectionSourceClass, Consumer<AbstractPayPalJUnit> configurer)
			throws Throwable {

		// Ensure mock PayPal
		Closure<HttpResponse<Order>> closure = new Closure<>();
		this.doOrder(sectionSourceClass, closure, configurer);

		// Ensure retrieved mocked order
		HttpResponse<Order> order = closure.value;
		JUnitAgnosticAssert.assertNotNull(order, "Should have order");
		return order;
	}

	/**
	 * Undertakes interacting with {@link Order}.
	 * 
	 * @param closure            Closure as parameter.
	 * @param sectionSourceClass {@link SectionSource} {@link Class}.
	 * @return {@link HttpResponse}.
	 */
	private <R> void doOrder(Class<?> sectionSourceClass, Closure<?> closure, Consumer<AbstractPayPalJUnit> configurer)
			throws Throwable {

		// Configure the rule
		configurer.accept(this.getPayPalJUnit());

		// Configure service
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add PayPal (mocked)
			office.addOfficeManagedObjectSource("PAYPAL", PayPalHttpClientManagedObjectSource.class.getName())
					.addOfficeManagedObject("PAYPAL", ManagedObjectScope.THREAD);

			// Should be overwritten by rule
			Singleton.load(office, new MockPayPalConfigurationRepository(
					new PayPalEnvironment.Sandbox("MOCK_CLIENT_ID", "MOCK_SECRET_ID")));

			// Add section
			context.addSection("SECTION", sectionSourceClass);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Trigger the servicing
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", closure);
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
