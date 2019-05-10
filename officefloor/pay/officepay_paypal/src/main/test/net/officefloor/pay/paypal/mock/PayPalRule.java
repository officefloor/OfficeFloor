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
package net.officefloor.pay.paypal.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.braintreepayments.http.Headers;
import com.braintreepayments.http.HttpRequest;
import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.serializer.Json;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;

import net.officefloor.pay.paypal.PayPalHttpClientManagedObjectSource;

/**
 * {@link TestRule} to mock {@link PayPalHttpClient} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalRule implements TestRule {

	/**
	 * {@link Json}.
	 */
	private static final Json JSON = new Json();

	/**
	 * Interaction.
	 */
	private class Interaction {

		/**
		 * {@link Predicate} to match {@link HttpRequest}.
		 */
		private final Predicate<HttpRequest<?>> matcher;

		/**
		 * {@link HttpResponse}.
		 */
		private final HttpResponse<?> response;

		/**
		 * Instantiate.
		 * 
		 * @param matcher  {@link Predicate} to match {@link HttpRequest}.
		 * @param response {@link HttpResponse}.
		 */
		private Interaction(Predicate<HttpRequest<?>> matcher, int statusCode, Object result,
				String[] headerNameValues) {
			this.matcher = matcher;
			Headers headers = new Headers();
			for (int i = 0; i < headerNameValues.length; i += 2) {
				headers.header(headerNameValues[i], headerNameValues[i + 1]);
			}
			this.response = new MockHttpResponse<>(headers, statusCode, result);
			PayPalRule.this.customInteractions.add(this);
		}
	}

	/**
	 * Custom {@link Interaction} instances.
	 */
	private final Deque<Interaction> customInteractions = new ConcurrentLinkedDeque<>();

	/**
	 * Adds an {@link Interaction}.
	 * 
	 * @param matcher          {@link Predicate} to match {@link HttpRequest}.
	 * @param statusCode       Status code.
	 * @param result           Result.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 */
	public void addInteraction(Predicate<HttpRequest<?>> matcher, int statusCode, Object result,
			String... headerNameValues) {
		new Interaction(matcher, statusCode, result, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCreateRequest} {@link Interaction}.
	 * 
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 */
	public void addOrdersCreateResponse(Order order, String... headerNameValues) {
		this.addOrdersCreateResponse((request) -> true, 200, order, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCreateRequest} {@link Interaction}.
	 * 
	 * @param matcher          {@link Predicate} to match
	 *                         {@link OrdersCreateRequest}.
	 * @param statusCode       Status code.
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 */
	public void addOrdersCreateResponse(Predicate<OrdersCreateRequest> matcher, int statusCode, Order order,
			String... headerNameValues) {
		new Interaction((request) -> {
			return (request instanceof OrdersCreateRequest) ? matcher.test((OrdersCreateRequest) request) : false;
		}, statusCode, order, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCaptureRequest} {@link Interaction}.
	 * 
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 */
	public void addOrdersCaptureResponse(Order order, String... headerNameValues) {
		this.addOrdersCaptureResponse((request) -> true, 200, order, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCaptureRequest} {@link Interaction}.
	 * 
	 * @param matcher          {@link Predicate} to match
	 *                         {@link OrdersCaptureRequest}.
	 * @param statusCode       Status code.
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 */
	public void addOrdersCaptureResponse(Predicate<OrdersCaptureRequest> matcher, int statusCode, Order order,
			String... headerNameValues) {
		new Interaction((request) -> {
			return (request instanceof OrdersCaptureRequest) ? matcher.test((OrdersCaptureRequest) request) : false;
		}, statusCode, order, headerNameValues);
	}

	/*
	 * ====================== TestRule =============================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Override the PayPal client with mock
				PayPalHttpClientManagedObjectSource.runWithPayPalHttpClient(new MockPayPalHttpClient(), () -> {

					// Evaluation the rule
					base.evaluate();
				});

				// Ensure all interactions have occurred
				assertEquals("Not all PayPal interactions exercised", 0, PayPalRule.this.customInteractions.size());
			}
		};
	}

	/**
	 * Mock {@link PayPalHttpClient}.
	 */
	private class MockPayPalHttpClient extends PayPalHttpClient {

		/**
		 * Instantiate.
		 */
		public MockPayPalHttpClient() {
			super(new PayPalEnvironment("MOCK_CLIENT", "MOCK_CLIENT", "http://mock.paypal.rule",
					"http://mock.paypal.rule"));
		}

		/*
		 * ==================== PayPalHttpClient ====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpResponse<T> execute(HttpRequest<T> request) throws IOException {

			// Determine if handle by configured interaction
			Iterator<Interaction> iterator = PayPalRule.this.customInteractions.iterator();
			while (iterator.hasNext()) {
				Interaction interaction = iterator.next();
				if (interaction.matcher.test(request)) {
					iterator.remove();
					return (HttpResponse<T>) interaction.response;
				}
			}

			// No interaction available
			Object body = request.requestBody();
			fail("No PayPal interaction for " + request.getClass().getSimpleName() + "("
					+ (body == null ? "<empty>" : JSON.serialize(body)) + ")");
			return null;
		}
	}

	/**
	 * Mock {@link HttpResponse}.
	 */
	private class MockHttpResponse<T> extends HttpResponse<T> {

		/**
		 * Instantiate.
		 * 
		 * @param headers    {@link Headers}.
		 * @param statusCode Status code.
		 * @param result     Result.
		 */
		private MockHttpResponse(Headers headers, int statusCode, T result) {
			super(headers, statusCode, result);
		}
	}

}