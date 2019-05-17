package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.PurchaseUnitRequest;

import net.officefloor.app.subscription.DomainLogic.CreatedOrder;
import net.officefloor.app.subscription.DomainLogic.DomainRequest;
import net.officefloor.app.subscription.store.Domain;
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

	private final ObjectifyRule obectify = new ObjectifyRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain chain = RuleChain.outerRule(this.jwt).around(this.payPal).around(this.obectify)
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
		User user = AuthenticateLogicTest.setupUser(this.obectify, "Daniel");
		String token = this.jwt.createAccessToken(user);
		MockHttpResponse response = this.server
				.send(MockWoofServer.mockRequest("/createDomainOrder").method(HttpMethod.POST)
						.header("authorization", "Bearer " + token).header("content-type", "application/json")
						.entity(mapper.writeValueAsString(new DomainRequest("officefloor.org"))));
		String entity = response.getEntity(null);
		assertEquals("Should be successful: " + entity, 200, response.getStatus().getStatusCode());
		CreatedOrder order = mapper.readValue(entity, CreatedOrder.class);
		assertEquals("Incorrect order ID", "MOCK_ORDER_ID", order.getOrderId());
		assertEquals("Incorrect status", "CREATED", order.getStatus());
		assertNotNull("Should have invoice", order.getInvoiceId());
		// TODO implement objectify.get(T, Function<LoadType<T>, LoadResult<T>>)
//		Domain domain = this.obectify.get(Domain.class, (loader) -> loader.id(Long.parseLong(order.getInvoiceId())));
		Domain domain = this.obectify.get(Domain.class, (loader) -> loader.filter("domain", "officefloor.org"));
		assertEquals("Incorrect invoiced domain", "officefloor.org", domain.getDomain());
		assertEquals("Incorrect invoiced user", user.getId(), domain.getUser().get().getId());
		assertNotNull("Should have invoice timestamp", domain.getTimestamp());
	}

}