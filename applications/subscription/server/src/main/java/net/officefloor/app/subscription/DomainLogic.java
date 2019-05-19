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
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;

import lombok.Value;
import net.officefloor.app.subscription.store.Domain;
import net.officefloor.app.subscription.store.Invoice;
import net.officefloor.app.subscription.store.Payment;
import net.officefloor.app.subscription.store.User;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
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
	public static class DomainOrderRequest {
		private String domain;
	}

	@Value
	public static class DomainCreatedOrder {
		private String orderId;
		private String status;
		private String invoiceId;
	}

	public static void createOrder(User user, DomainOrderRequest request, Objectify objectify, PayPalHttpClient paypal,
			ObjectResponse<DomainCreatedOrder> response) throws IOException {

		// TODO validate the domain

		// Create the invoice entry
		String domainName = request.getDomain();
		Invoice invoice = new Invoice(Ref.create(user), Invoice.PRODUCT_TYPE_DOMAIN, domainName);
		objectify.save().entities(invoice).now();
		String invoiceId = String.valueOf(invoice.getId());

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

		// Update the invoice with the order
		String paymentOrderId = order.id();
		invoice.setPaymentOrderId(paymentOrderId);
		objectify.save().entity(invoice).now();

		// Send the response
		response.send(new DomainCreatedOrder(order.id(), order.status(), invoiceId));
	}

	@Value
	@HttpObject
	public static class DomainCaptureRequest {
		private String orderId;
	}

	@Value
	public static class DomainCapturedOrder {
		private String orderId;
		private String status;
		private String domain;
	}

	public static void captureOrder(User user, DomainCaptureRequest request, Objectify objectify,
			PayPalHttpClient paypal, ObjectResponse<DomainCapturedOrder> response) throws IOException {

		// Obtain the order id
		String orderId = request.getOrderId();

		// Obtain the invoice
		Invoice invoice = objectify.load().type(Invoice.class).filter("paymentOrderId", orderId).first().now();
		String domainName = invoice.getProductReference();

		// Capture the funds
		HttpResponse<Order> orderResponse = paypal.execute(new OrdersCaptureRequest(orderId));
		Order order = orderResponse.result();
		String captureStatus = order.status();
		if (!"COMPLETED".equalsIgnoreCase(captureStatus)) {
			throw new HttpException(HttpStatus.PAYMENT_REQUIRED);
		}

		// TODO extract amount and receipt from response
		int amount = 500;
		String receipt = "CAPTURE_ID";

		// Funds captured, so create entries
		Ref<User> userRef = Ref.create(user);
		Ref<Invoice> invoiceRef = Ref.create(invoice);
		Domain domain = new Domain(domainName, userRef, invoiceRef);
		Payment payment = new Payment(userRef, invoiceRef, amount, receipt);
		objectify.save().entities(domain, payment).now();

		// Send the response
		response.send(new DomainCapturedOrder(orderId, captureStatus, domainName));
	}

}