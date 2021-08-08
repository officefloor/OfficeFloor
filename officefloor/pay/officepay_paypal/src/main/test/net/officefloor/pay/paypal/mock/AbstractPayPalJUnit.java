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
import java.io.StringWriter;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.Headers;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;

import net.officefloor.pay.paypal.PayPalHttpClientManagedObjectSource;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Abstract JUnit functionality to mock {@link PayPalHttpClient} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractPayPalJUnit {

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
			AbstractPayPalJUnit.this.customInteractions.add(this);
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
			AbstractPayPalJUnit.this.customInteractions.add(this);
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
	 * {@link MockPayPalHttpClient}.
	 */
	private MockPayPalHttpClient client;

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

	/**
	 * Asserts the interactions.
	 * 
	 * @throws Exception If fails assertion.
	 */
	public void assertInteractions() throws Exception {

		// Ensure no validation error
		Throwable failure = client.firstValidationFailure;
		if (failure != null) {

			// Clear failure, as propagating
			client.firstValidationFailure = null;

			// Propagate the failure
			if (failure instanceof Exception) {
				throw (Exception) failure;
			} else {
				JUnitAgnosticAssert.fail(failure);
			}
		}

		// Ensure all interactions have occurred
		int interactionCount = this.customInteractions.size();
		if (interactionCount > 0) {

			// Obtain the custom interaction information
			StringWriter buffer = new StringWriter();
			buffer.write("Not all PayPal interactions exercised");
			for (Interaction<?> interaction : AbstractPayPalJUnit.this.customInteractions) {
				buffer.write("\n\nRESPONSE: ");
				buffer.write(JSON.serialize(interaction.response.result()));
			}

			// Clear the interactions (as asserted)
			this.customInteractions.clear();

			// Assert incorrect number of interactions
			JUnitAgnosticAssert.assertEquals(0, interactionCount, buffer.toString());
		}
	}

	/**
	 * Sets up the mock PayPal HTTP client.
	 */
	protected void setupMockPayPaylHttpClient() {
		// Override the PayPal client with mock
		this.client = new MockPayPalHttpClient();
		PayPalHttpClientManagedObjectSource.setPayPalHttpClient(this.client);
	}

	/**
	 * Tears down the mock PayPal HTTP client.
	 */
	protected void tearDownMockPayPaylHttpClient() throws Exception {
		try {
			// Remove the PayPal mock
			PayPalHttpClientManagedObjectSource.setPayPalHttpClient(null);

			// Ensure no validation issues
			this.assertInteractions();

		} finally {
			// Ensure clear client
			this.client = null;
		}
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
			Iterator<Interaction<?>> iterator = AbstractPayPalJUnit.this.customInteractions.iterator();
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
			JUnitAgnosticAssert.fail("No PayPal interaction for " + request.getClass().getSimpleName() + "("
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
