package net.officefloor.pay.paypal;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.runners.model.Statement;

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
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.pay.paypal.mock.PayPalRule;
import net.officefloor.pay.paypal.mock.PayPalRule.Interaction;
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
public class PayPalHttpClientTest extends OfficeFrameTestCase {

	private static final Json JSON = new Json();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	private MockWoofServer mockServer;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
		if (this.mockServer != null) {
			this.mockServer.close();
		}
	}

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(PayPalHttpClientManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(PayPalHttpClient.class);
		type.addFunctionDependency(PayPalConfigurationRepository.class.getSimpleName(),
				PayPalConfigurationRepository.class);
		type.setInput(true);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, PayPalHttpClientManagedObjectSource.class);
	}

	/**
	 * Ensure can point to particular {@link PayPalEnvironment}.
	 */
	public void testConfigureEnvironment() throws Throwable {

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
			PayPalHttpClientTest.this.officeFloor = compiler.compileAndOpenOfficeFloor();

			// Trigger the servicing
			Closure<HttpResponse<Order>> closure = new Closure<>();
			CompileOfficeFloor.invokeProcess(PayPalHttpClientTest.this.officeFloor, "SECTION.service", closure);

			// Ensure have order
			Order order = closure.value.result();
			assertNotNull("Should have order", order);
			assertEquals("Incorrect order id", "ORDER_ID", order.id());
			assertEquals("Incorrect status", "COMPLETED", order.status());
		}
	}

	public static class ConfigureEnvironmentService {
		public void service(@Parameter Closure<HttpResponse<Order>> closure, PayPalHttpClient client)
				throws IOException {
			closure.value = MockCreateOrderService.createOrder(client);
		}
	}

	public static class MockPayPalService {

		@MockWoofInput(method = "POST", path = "/v1/oauth2/token")
		public void auth(ServerHttpConnection connection) throws IOException {

			// Ensure correct environment details
			BasicCredentials credentials = BasicHttpSecuritySource.getBasicCredentials(connection.getRequest());
			assertNotNull("Should have credentials", credentials);
			assertEquals("Incorrect client id", "MOCK_CLIENT_ID", credentials.userId);
			assertEquals("Incorrect client secret", "MOCK_CLIENT_SECRET", credentials.password);

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

	/**
	 * Ensure can mock creating an {@link Order}.
	 */
	public void testMockCreateOrder() throws Throwable {
		HttpResponse<Order> response = this.doOrder(MockCreateOrderService.class,
				(rule) -> rule.addOrdersCreateResponse(new Order().id("ORDER_ID").status("CREATED"), "TEST", "VALUE"));
		assertEquals("Incorrect header", "VALUE", response.headers().header("TEST"));
		assertEquals("Incorrect order Id", "ORDER_ID", response.result().id());
		assertEquals("Incorrect status", "CREATED", response.result().status());
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
			assertEquals("Should be successful", 200, orderResponse.statusCode());
			return orderResponse;
		}
	}

	/**
	 * Ensure can mock capture {@link Order}.
	 */
	public void testMockCaptureOrder() throws Throwable {
		Closure<String> orderId = new Closure<>();
		HttpResponse<Order> response = this.doOrder(MockCaptureService.class, (rule) -> rule
				.addOrdersCaptureResponse(new Order().id("ORDER_ID").status("COMPLETED")).validate((request) -> {
					orderId.value = rule.getOrderId(request);
				}));
		assertEquals("Incorrect order Id", "ORDER_ID", response.result().id());
		assertEquals("Incorrect status", "COMPLETED", response.result().status());
		assertEquals("Incorrect request order Id", "ORDER_ID", orderId.value);
	}

	public static class MockCaptureService {
		public void service(@Parameter Closure<HttpResponse<Order>> closure, PayPalHttpClient client)
				throws IOException {
			OrdersCaptureRequest request = new OrdersCaptureRequest("ORDER_ID");
			request.requestBody(new OrderRequest());
			HttpResponse<Order> orderResponse = client.execute(request);
			assertEquals("Should be successful", 200, orderResponse.statusCode());
			closure.value = orderResponse;
		}
	}

	/**
	 * Ensure report unexpected {@link Interaction}.
	 */
	public void testUnexpectedInteraction() throws Throwable {
		try {
			this.doOrder(MockCreateOrderService.class, (rule) -> {
				// No interaction, so is unexpected
			});
			fail("Should not be successful");
		} catch (AssertionError ex) {
			assertEquals("Incorrect cause",
					"No PayPal interaction for OrdersCreateRequest("
							+ JSON.serialize(MockCreateOrderService.createOrdersRequest().requestBody()) + ")",
					ex.getMessage());
		}
	}

	/**
	 * Ensure all {@link Interaction} instances occur.
	 */
	public void testMissingInteraction() throws Throwable {
		Order unexpected = new Order().status("MISSING");
		try {
			this.doOrder(MockCreateOrderService.class, (rule) -> {
				rule.addOrdersCreateResponse(new Order().status("USED"));

				// Record missing interaction
				rule.addOrdersCaptureResponse(unexpected);
			});
			fail("Should not be successful");
		} catch (AssertionError ex) {
			assertEquals("Incorrect cause", "Not all PayPal interactions exercised\n\nRESPONSE: "
					+ JSON.serialize(unexpected) + " expected:<0> but was:<1>", ex.getMessage());
		}
	}

	/**
	 * Ensure can validate the {@link HttpRequest}.
	 */
	public void testValidateHttpRequest() throws Throwable {
		final Exception failure = new Exception("TEST");
		try {
			this.doOrder(MockCreateOrderService.class,
					(rule) -> rule.addOrdersCreateResponse(new Order()).validate((request) -> {
						OrderRequest orderRequest = (OrderRequest) request.requestBody();
						assertEquals("Incorrect intent", "CAPTURE", orderRequest.checkoutPaymentIntent());
						throw failure;
					}));
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Should throw failure", failure, ex);
		}
	}

	/**
	 * Ensure can mock PayPal error.
	 */
	public void testPayPalError() throws Throwable {
		final IOException failure = new IOException("TEST");
		Closure<Exception> closure = new Closure<>();
		this.doOrder(MockErrorService.class, closure, (rule) -> rule.addException(failure));
		assertSame("Should throw failure", failure, closure.value);
	}

	public static class MockErrorService {
		public void service(@Parameter Closure<Exception> closure, PayPalHttpClient client) throws IOException {
			OrdersCaptureRequest request = new OrdersCaptureRequest("ORDER_ID");
			request.requestBody(new OrderRequest());
			try {
				client.execute(request);
				fail("Should not be successful");
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
	private HttpResponse<Order> doOrder(Class<?> sectionSourceClass, Consumer<PayPalRule> configurer) throws Throwable {

		// Ensure mock PayPal
		Closure<HttpResponse<Order>> closure = new Closure<>();
		this.doOrder(sectionSourceClass, closure, configurer);

		// Ensure retrieved mocked order
		HttpResponse<Order> order = closure.value;
		assertNotNull("Should have order", order);
		return order;
	}

	/**
	 * Undertakes interacting with {@link Order}.
	 * 
	 * @param closure            Closure as parameter.
	 * @param sectionSourceClass {@link SectionSource} {@link Class}.
	 * @return {@link HttpResponse}.
	 */
	private <R> void doOrder(Class<?> sectionSourceClass, Closure<?> closure, Consumer<PayPalRule> configurer)
			throws Throwable {

		// Ensure mock PayPal
		PayPalRule rule = new PayPalRule();
		rule.apply(new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Configure the rule
				configurer.accept(rule);

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
				PayPalHttpClientTest.this.officeFloor = compiler.compileAndOpenOfficeFloor();

				// Trigger the servicing
				CompileOfficeFloor.invokeProcess(PayPalHttpClientTest.this.officeFloor, "SECTION.service", closure);
			}
		}, null).evaluate();
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