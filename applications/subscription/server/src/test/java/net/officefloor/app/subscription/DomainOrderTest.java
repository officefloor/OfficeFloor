package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.PurchaseUnitRequest;

import net.officefloor.app.subscription.jwt.JwtClaims;
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

	private final MockJwtAccessTokenRule jwt = new MockJwtAccessTokenRule();

	private final PayPalRule payPal = new PayPalRule();

	private final ObjectifyRule obectify = new ObjectifyRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain chain = RuleChain.outerRule(this.jwt).around(this.payPal).around(this.obectify)
			.around(this.server);

	@Test
	public void createDomainOrder() {

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
			assertEquals("5.00", purchase.amount());
			assertEquals("4.54", purchase.amount().breakdown().itemTotal().value());
			assertEquals("AUD", purchase.amount().breakdown().itemTotal().currencyCode());
			assertEquals("0.46", purchase.amount().breakdown().taxTotal().value());
			assertEquals("AUD", purchase.amount().breakdown().taxTotal().currencyCode());
			assertEquals("OfficeFloor domain subscription officefloor.org", purchase.description());
			assertEquals("OfficeFloor domain", purchase.softDescriptor());
			assertNotNull(purchase.customId());
			assertNotNull(purchase.invoiceId());
			assertEquals("Should only be one item", 1, purchase.items().size());
			Item item = purchase.items().get(0);
			assertEquals("Domain subscription officefloor.org", item.name());
			assertEquals("4.54", item.unitAmount().value());
			assertEquals("AUD", item.unitAmount().currencyCode());
			assertEquals("0.56", item.tax().value());
			assertEquals("AUD", item.tax().value());
			assertEquals("1", item.quantity());
			assertEquals("DIGITAL_GOODS", item.category());
			assertEquals("http:/officefloor.org", item.url());
		});

		// Send request
		User user = AuthenticateLogicTest.setupUser(this.obectify, "Daniel");
		String token = this.jwt.createAccessToken(new JwtClaims(user.getId(), new String[0]));
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/createDomainOrder")
				.method(HttpMethod.POST).header("authorization", "Bearer " + token));
		response.assertResponse(204, "");
	}

}