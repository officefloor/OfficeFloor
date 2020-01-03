package net.officefloor.tutorial.paypalhttpserver;

import java.io.IOException;
import java.util.Arrays;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.Item;
import com.paypal.orders.Money;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;

import lombok.Value;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * PayPal logic.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalLogic {

	// START SNIPPET: configure
	@Value
	@HttpObject
	public static class Configuration {
		private String clientId;
		private String clientSecret;
	}

	public void configure(Configuration configuration, InMemoryPayPalConfigurationRepository repository) {
		repository.loadEnvironment(configuration.clientId, configuration.clientSecret);
	}
	// END SNIPPET: configure

	// START SNIPPET: create
	@Value
	@HttpObject
	public static class CreateOrder {
		private String currency;
	}

	@Value
	public static class CreatedOrder {
		private String orderId;
		private String status;
	}

	public void createOrder(CreateOrder createOrder, PayPalHttpClient client, ObjectResponse<CreatedOrder> response)
			throws IOException {
		String currency = createOrder.getCurrency();
		HttpResponse<Order> order = client
				.execute(new OrdersCreateRequest().requestBody(new OrderRequest().checkoutPaymentIntent("CAPTURE")
						.purchaseUnits(Arrays.asList(new PurchaseUnitRequest().description("Test create order")
								.amountWithBreakdown(new AmountWithBreakdown().currencyCode(currency).value("5.00")
										.amountBreakdown(new AmountBreakdown()
												.itemTotal(new Money().currencyCode(currency).value("4.50"))
												.taxTotal(new Money().currencyCode(currency).value("0.50"))))
								.items(Arrays.asList(new Item().name("Domain").description("Domain subscription")
										.unitAmount(new Money().currencyCode(currency).value("4.50"))
										.tax(new Money().currencyCode(currency).value("0.50")).quantity("1")))))));
		response.send(new CreatedOrder(order.result().id(), order.result().status()));
	}
	// END SNIPPET: create

	// START SNIPPET: capture
	@Value
	@HttpObject
	public static class CaptureOrder {
		private String orderId;
	}

	@Value
	public static class CapturedOrder {
		private String orderId;
		private String status;
	}

	public void captureOrder(CaptureOrder captureOrder, PayPalHttpClient client, ObjectResponse<CapturedOrder> response)
			throws IOException {
		OrdersCaptureRequest request = new OrdersCaptureRequest(captureOrder.orderId);
		request.requestBody(new OrderRequest());
		HttpResponse<Order> order = client.execute(request);
		response.send(new CapturedOrder(order.result().id(), order.result().status()));
	}
	// END SNIPPET: capture
}