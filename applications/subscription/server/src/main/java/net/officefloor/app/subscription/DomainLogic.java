package net.officefloor.app.subscription;

import java.io.IOException;
import java.util.Arrays;

import com.braintreepayments.http.HttpResponse;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Ref;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Money;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;

import lombok.Value;
import net.officefloor.app.subscription.store.Domain;
import net.officefloor.app.subscription.store.User;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * Logic to retrieve the domain entries.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainLogic {

	private static final String SUBSCRIPTION_VALUE = "4.54";
	private static final String SUBSCRIPTION_TAX = "0.46";
	private static final String SUBSCRIPTION_TOTAL = "5.00";
	private static final String CURRENCY = "AUD";

	@Value
	@HttpObject
	public static class DomainRequest {
		private String domain;
	}

	@Value
	public static class CreatedOrder {
		private String orderId;
		private String status;
		private String invoiceId;
	}

	public static void createOrder(User user, DomainRequest request, Objectify objectify, PayPalHttpClient paypal,
			ObjectResponse<CreatedOrder> response) throws IOException {

		// TODO validate the domain

		// Create the domain entry (flagged to be paid)
		String domainName = request.getDomain();
		Domain domain = new Domain(domainName, Ref.create(user));
		objectify.save().entities(domain).now();
		String invoiceId = String.valueOf(domain.getId());

		// Create order for the domain
		HttpResponse<Order> orderResponse = paypal.execute(new OrdersCreateRequest().requestBody(new OrderRequest()
				.intent("CAPTURE")
				.applicationContext(new ApplicationContext().shippingPreference("NO_SHIPPING").userAction("PAY_NOW"))
				.purchaseUnits(Arrays
						.asList(new PurchaseUnitRequest().description("OfficeFloor domain subscription " + domainName)
								.softDescriptor("OfficeFloor domain").invoiceId(invoiceId)
								.amount(new AmountWithBreakdown().value(SUBSCRIPTION_TOTAL).currencyCode(CURRENCY)
										.breakdown(new AmountBreakdown()
												.itemTotal(new Money().value(SUBSCRIPTION_VALUE).currencyCode(CURRENCY))
												.taxTotal(new Money().value(SUBSCRIPTION_TAX).currencyCode(CURRENCY))))
								.items(Arrays.asList(new Item().name("Subscription")
										.description("Domain subscription " + domainName)
										.unitAmount(new Money().value(SUBSCRIPTION_VALUE).currencyCode(CURRENCY))
										.tax(new Money().value(SUBSCRIPTION_TAX).currencyCode(CURRENCY)).quantity("1")
										.category("DIGITAL_GOODS").url("http://" + domainName)))))));
		Order order = orderResponse.result();
		response.send(new CreatedOrder(order.id(), order.status(), invoiceId));
	}

}