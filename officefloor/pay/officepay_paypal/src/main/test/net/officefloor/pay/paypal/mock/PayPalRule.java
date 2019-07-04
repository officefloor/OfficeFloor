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
import java.io.StringWriter;
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
	 * Validates the {@link HttpRequest}.
	 */
	@FunctionalInterface
	public static interface Validator<R extends HttpRequest<? extends Object>> {

		/**
		 * Validates the {@link HttpRequest}.
		 * 
		 * @param request {@link HttpRequest}.
		 * @throws Throwable Possible validation failure.
		 */
		void validate(R request) throws Throwable;
	}

	/**
	 * Interaction.
	 */
	public class Interaction<R extends HttpRequest<? extends Object>> {

		/**
		 * {@link Predicate} to match {@link HttpRequest}.
		 */
		private final Predicate<Object> matcher;

		/**
		 * {@link HttpResponse}.
		 */
		private final HttpResponse<?> response;

		/**
		 * {@link IOException}.
		 */
		private final IOException exception;

		/**
		 * {@link Validator}.
		 */
		private Validator<R> validator = null;

		/**
		 * Instantiate.
		 * 
		 * @param matcher  {@link Predicate} to match {@link HttpRequest}.
		 * @param response {@link HttpResponse}.
		 */
		private Interaction(Predicate<Object> matcher, int statusCode, Object result, String[] headerNameValues) {
			this.matcher = matcher;
			Headers headers = new Headers();
			for (int i = 0; i < headerNameValues.length; i += 2) {
				headers.header(headerNameValues[i], headerNameValues[i + 1]);
			}
			this.response = new MockHttpResponse<>(headers, statusCode, result);
			this.exception = null;
			PayPalRule.this.customInteractions.add(this);
		}

		/**
		 * Instantiate.
		 * 
		 * @param matcher   {@link Predicate} to match {@link HttpRequest}.
		 * @param exception {@link IOException}.
		 */
		private Interaction(Predicate<Object> matcher, IOException exception) {
			this.matcher = matcher;
			this.response = null;
			this.exception = exception;
			PayPalRule.this.customInteractions.add(this);
		}

		/**
		 * Registers a {@link Validator}.
		 * 
		 * @param validator {@link Validator}.
		 * @return <code>this</code>.
		 */
		public Interaction<R> validate(Validator<R> validator) {
			this.validator = validator;
			return this;
		}
	}

	/**
	 * Custom {@link Interaction} instances.
	 */
	private final Deque<Interaction<?>> customInteractions = new ConcurrentLinkedDeque<>();

	/**
	 * Adds an {@link Interaction}.
	 * 
	 * @param matcher          {@link Predicate} to match {@link HttpRequest}.
	 * @param statusCode       Status code.
	 * @param result           Result.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 * @return {@link Interaction}.
	 */
	public <R extends HttpRequest<? extends Object>> Interaction<R> addInteraction(Predicate<Object> matcher,
			int statusCode, Object result, String... headerNameValues) {
		return new Interaction<>(matcher, statusCode, result, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCreateRequest} {@link Interaction}.
	 * 
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 * @return {@link Interaction}.
	 */
	public Interaction<OrdersCreateRequest> addOrdersCreateResponse(Order order, String... headerNameValues) {
		return this.addOrdersCreateResponse((request) -> true, 200, order, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCreateRequest} {@link Interaction}.
	 * 
	 * @param matcher          {@link Predicate} to match
	 *                         {@link OrdersCreateRequest}.
	 * @param statusCode       Status code.
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 * @return {@link Interaction}.
	 */
	public Interaction<OrdersCreateRequest> addOrdersCreateResponse(Predicate<OrdersCreateRequest> matcher,
			int statusCode, Order order, String... headerNameValues) {
		return new Interaction<>((request) -> {
			return (request instanceof OrdersCreateRequest) ? matcher.test((OrdersCreateRequest) request) : false;
		}, statusCode, order, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCaptureRequest} {@link Interaction}.
	 * 
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 * @return {@link Interaction}.
	 */
	public Interaction<OrdersCaptureRequest> addOrdersCaptureResponse(Order order, String... headerNameValues) {
		return this.addOrdersCaptureResponse((request) -> true, 200, order, headerNameValues);
	}

	/**
	 * Adds an {@link OrdersCaptureRequest} {@link Interaction}.
	 * 
	 * @param matcher          {@link Predicate} to match
	 *                         {@link OrdersCaptureRequest}.
	 * @param statusCode       Status code.
	 * @param order            {@link Order}.
	 * @param headerNameValues {@link Headers} name/value pairs.
	 * @return {@link Interaction}.
	 */
	public Interaction<OrdersCaptureRequest> addOrdersCaptureResponse(Predicate<OrdersCaptureRequest> matcher,
			int statusCode, Order order, String... headerNameValues) {
		return new Interaction<>((request) -> {
			return (request instanceof OrdersCaptureRequest) ? matcher.test((OrdersCaptureRequest) request) : false;
		}, statusCode, order, headerNameValues);
	}

	/**
	 * Adds a failure {@link Interaction}.
	 * 
	 * @param exception {@link IOException}.
	 */
	public void addException(IOException exception) {
		this.addException((request) -> true, exception);
	}

	/**
	 * Adds a failure {@link Interaction}.
	 * 
	 * @param matcher   {@link Predicate} to match {@link HttpRequest}.
	 * @param exception {@link IOException}.
	 */
	@SuppressWarnings("unchecked")
	public void addException(Predicate<HttpRequest<Order>> matcher, IOException exception) {
		new Interaction<>((request) -> {
			return (request instanceof HttpRequest) ? matcher.test((HttpRequest<Order>) request) : false;
		}, exception);
	}

	/**
	 * Extracts the {@link Order} id from the {@link OrdersCaptureRequest}.
	 * 
	 * @param request {@link OrdersCaptureRequest}.
	 * @return {@link Order} id.
	 */
	public String getOrderId(OrdersCaptureRequest request) {
		String path = request.path();
		path = path.substring("/v2/checkout/orders/".length());
		path = path.substring(0, path.indexOf("/capture"));
		return path;
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
				MockPayPalHttpClient client = new MockPayPalHttpClient();
				PayPalHttpClientManagedObjectSource.runWithPayPalHttpClient(client, () -> {

					// Evaluation the rule
					base.evaluate();
				});

				// Ensure no validation error
				if (client.firstValidationFailure != null) {
					throw client.firstValidationFailure;
				}

				// Ensure all interactions have occurred
				if (PayPalRule.this.customInteractions.size() > 0) {
					StringWriter buffer = new StringWriter();
					buffer.write("Not all PayPal interactions exercised");
					for (Interaction<?> interaction : PayPalRule.this.customInteractions) {
						buffer.write("\n\nRESPONSE: ");
						buffer.write(JSON.serialize(interaction.response.result()));
					}
					assertEquals(buffer.toString(), 0, PayPalRule.this.customInteractions.size());
				}
			}
		};
	}

	/**
	 * Mock {@link PayPalHttpClient}.
	 */
	private class MockPayPalHttpClient extends PayPalHttpClient {

		/**
		 * First possible validation failure.
		 */
		private volatile Throwable firstValidationFailure = null;

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
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T> HttpResponse<T> execute(HttpRequest<T> request) throws IOException {

			// Determine if handle by configured interaction
			Iterator<Interaction<?>> iterator = PayPalRule.this.customInteractions.iterator();
			while (iterator.hasNext()) {
				Interaction interaction = iterator.next();
				if (interaction.matcher.test(request)) {
					iterator.remove();

					// Determine if exception
					if (interaction.exception != null) {
						throw interaction.exception;
					}

					// Ensure valid
					if (interaction.validator != null) {
						try {
							interaction.validator.validate(request);
						} catch (Throwable ex) {
							if (this.firstValidationFailure == null) {
								this.firstValidationFailure = ex;
							}
						}
					}

					// Return the response
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