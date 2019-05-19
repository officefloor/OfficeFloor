package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.Ref;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.PurchaseUnitRequest;

import net.officefloor.app.subscription.DomainLogic.DomainCreatedOrder;
import net.officefloor.app.subscription.DomainLogic.DomainCaptureRequest;
import net.officefloor.app.subscription.DomainLogic.DomainCapturedOrder;
import net.officefloor.app.subscription.DomainLogic.DomainOrderRequest;
import net.officefloor.app.subscription.store.Domain;
import net.officefloor.app.subscription.store.Invoice;
import net.officefloor.app.subscription.store.Payment;
import net.officefloor.app.subscription.store.User;
import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.pay.paypal.mock.PayPalRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.web.jwt.mock.MockJwtAccessTokenRule;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests creating the PayPal order for a domain.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainOrderTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final MockJwtAccessTokenRule jwt = new MockJwtAccessTokenRule();

	private final PayPalRule payPal = new PayPalRule();

	private final ObjectifyRule objectify = new ObjectifyRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain chain = RuleChain.outerRule(this.jwt).around(this.payPal).around(this.objectify)
			.around(this.server);

	@Test
	public void createDomainOrder() throws Exception {

		// Record
		this.payPal.addOrdersCreateResponse(new Order().id("MOCK_ORDER_ID").status("CREATED")).validate((request) -> {
			OrderRequest order = (OrderRequest) request.requestBody();
			assertEquals("CAPTURE", order.intent());
			ApplicationContext appContext = order.applicationContext();
			assertEquals("NO_SHIPPING", appContext.shippingPreference());
			assertEquals("PAY_NOW", appContext.userAction());
			List<PurchaseUnitRequest> purchaseUnits = order.purchaseUnits();
			assertEquals("Should be one domain purchased", 1, purchaseUnits.size());
			PurchaseUnitRequest purchase = purchaseUnits.get(0);
			assertEquals("OfficeFloor domain subscription officefloor.org", purchase.description());
			assertEquals("OfficeFloor domain", purchase.softDescriptor());
			assertNotNull(purchase.invoiceId());
			assertEquals("5.00", purchase.amount().value());
			assertEquals("AUD", purchase.amount().currencyCode());
			assertEquals("4.54", purchase.amount().breakdown().itemTotal().value());
			assertEquals("AUD", purchase.amount().breakdown().itemTotal().currencyCode());
			assertEquals("0.46", purchase.amount().breakdown().taxTotal().value());
			assertEquals("AUD", purchase.amount().breakdown().taxTotal().currencyCode());
			assertEquals("Should only be one item", 1, purchase.items().size());
			Item item = purchase.items().get(0);
			assertEquals("Subscription", item.name());
			assertEquals("Domain subscription officefloor.org", item.description());
			assertEquals("4.54", item.unitAmount().value());
			assertEquals("AUD", item.unitAmount().currencyCode());
			assertEquals("0.46", item.tax().value());
			assertEquals("AUD", item.tax().currencyCode());
			assertEquals("1", item.quantity());
			assertEquals("DIGITAL_GOODS", item.category());
			assertEquals("http://officefloor.org", item.url());
		});

		// Send request
		User user = AuthenticateLogicTest.setupUser(this.objectify, "Daniel");
		String token = this.jwt.createAccessToken(user);
		MockHttpResponse response = this.server
				.send(MockWoofServer.mockRequest("/createDomainOrder").method(HttpMethod.POST)
						.header("authorization", "Bearer " + token).header("content-type", "application/json")
						.entity(mapper.writeValueAsString(new DomainOrderRequest("officefloor.org"))));

		// Ensure correct response
		String entity = response.getEntity(null);
		assertEquals("Should be successful: " + entity, 200, response.getStatus().getStatusCode());
		DomainCreatedOrder order = mapper.readValue(entity, DomainCreatedOrder.class);
		assertEquals("Incorrect order ID", "MOCK_ORDER_ID", order.getOrderId());
		assertEquals("Incorrect status", "CREATED", order.getStatus());
		assertNotNull("Should have invoice", order.getInvoiceId());

		// Ensure invoice captured in data store
		Invoice invoice = this.objectify.get(Invoice.class, Long.parseLong(order.getInvoiceId()));
		assertEquals("Incorrect invoiced user", user.getId(), invoice.getUser().get().getId());
		assertEquals("Incorrect product type", Invoice.PRODUCT_TYPE_DOMAIN, invoice.getProductType());
		assertEquals("Incorrect invoiced domain", "officefloor.org", invoice.getProductReference());
		assertEquals("Incorrect payment order id", "MOCK_ORDER_ID", invoice.getPaymentOrderId());
		assertNotNull("Should have invoice timestamp", invoice.getTimestamp());
	}

	@Test
	public void captureDomainOrder() throws Exception {

		// Record
		this.payPal.addOrdersCaptureResponse(new Order().id("MOCK_ORDER_ID").status("COMPLETED"))
				.validate((request) -> {
					assertEquals("MOCK_ORDER_ID", this.payPal.getOrderId(request));
				});

		// Setup the invoice
		User user = AuthenticateLogicTest.setupUser(this.objectify, "Daniel");
		Invoice invoice = new Invoice(Ref.create(user), Invoice.PRODUCT_TYPE_DOMAIN, "officefloor.org");
		invoice.setPaymentOrderId("MOCK_ORDER_ID");
		this.objectify.store(invoice);

		// Send request
		String token = this.jwt.createAccessToken(user);
		MockHttpResponse response = this.server
				.send(MockWoofServer.mockRequest("/captureDomainOrder").method(HttpMethod.POST)
						.header("authorization", "Bearer " + token).header("content-type", "application/json")
						.entity(mapper.writeValueAsString(new DomainCaptureRequest("MOCK_ORDER_ID"))));

		// Ensure correct response
		String entity = response.getEntity(null);
		assertEquals("Should be successful: " + entity, 200, response.getStatus().getStatusCode());
		DomainCapturedOrder order = mapper.readValue(entity, DomainCapturedOrder.class);
		assertEquals("Incorrect order ID", "MOCK_ORDER_ID", order.getOrderId());
		assertEquals("Incorrect status", "COMPLETED", order.getStatus());
		assertEquals("Incorrect domain", "officefloor.org", order.getDomain());

		// Ensure domain capture in data store
		Domain domain = this.objectify.get(Domain.class, 1, (loader) -> loader.filter("domain", "officefloor.org"))
				.get(0);
		assertEquals("Incorrect domain on domain", "officefloor.org", domain.getDomain());
		assertEquals("Incorrect user for domain", user.getId(), domain.getUser().get().getId());
		assertEquals("Incorrect invoice for domain", invoice.getId(), domain.getInvoice().get().getId());
		assertNotNull("Should have domain timestamp", domain.getTimestamp());

		// Ensure payment captured
		Payment payment = this.objectify.get(Payment.class, 1, (loader) -> loader.filter("invoice", invoice)).get(0);
		assertEquals("Incorrect user for payment", user.getId(), payment.getUser().get().getId());
		assertEquals("Incorrect amount", Integer.valueOf(5_00), payment.getAmount());
		assertEquals("Incorrect receipt", "CAPTURE_ID", payment.getReceipt());
	}

}