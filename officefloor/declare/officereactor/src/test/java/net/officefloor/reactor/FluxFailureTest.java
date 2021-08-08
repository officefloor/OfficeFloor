/*-
 * #%L
 * Reactor
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

package net.officefloor.reactor;

import reactor.core.publisher.Flux;

/**
 * Tests failure of {@link Flux}.
 * 
 * @author Daniel Sagenschneider
 */
public class FluxFailureTest extends AbstractReactorTestCase {

	private static Throwable exception = new Throwable("TEST");

	/**
	 * Ensure handle {@link Throwable}.
	 */
	public void testThrowable() {
		this.valid(exception);
	}

	/**
	 * Ensure handle {@link Exception}.
	 */
	public void testException() {
		this.valid(new Exception("TEST"));
	}

	/**
	 * Ensure handle {@link RuntimeException}.
	 */
	public void testRuntimeException() {
		this.valid(new RuntimeException("TEST"));
	}

	/**
	 * Ensure handle {@link Error}.
	 */
	public void testError() {
		this.valid(new Error("TEST"));
	}

	public Flux<Object> failure() {
		return Flux.error(exception);
	}

	/**
	 * Ensure only the first Error.
	 */
	public void testErrors() {
		this.valid("errors", exception);
	}

	public Flux<Object> errors() {
		return Flux.just("ONE", "TWO", "THREE").flatMap(
				value -> "ONE".equals(value) ? Flux.error(exception) : Flux.error(new RuntimeException("OTHER")));
	}

	/**
	 * Ensure handle successes then {@link Exception}.
	 */
	public void testSuccessesThenError() {
		this.valid("successesThenError", exception);
	}

	public Flux<Object> successesThenError() {
		return Flux.just("ONE", "TWO", "THREE", "ERROR")
				.flatMap(value -> "ERROR".equals(value) ? Flux.error(exception) : Flux.just(value));
	}

	/**
	 * Ensure handle {@link Exception} then ignore success.
	 */
	public void testErrorThenIgnoreSuccesses() {
		this.valid("errorThenIgnoreSuccesses", exception);
	}

	public Flux<Object> errorThenIgnoreSuccesses() {
		return Flux.just("ERROR", "ONE", "TWO", "THREE")
				.flatMap(value -> "ERROR".equals(value) ? Flux.error(exception) : Flux.just(value));
	}

	/**
	 * Ensure handle {@link Exception} mid stream.
	 */
	public void testErrorMidStream() {
		this.valid("errorMidStream", exception);
	}

	public Flux<Object> errorMidStream() {
		return Flux.just("ONE", "TWO", "ERROR", "THREE")
				.flatMap(value -> "ERROR".equals(value) ? Flux.error(exception) : Flux.just(value));
	}

	/**
	 * Valid handling of failure.
	 */
	protected void valid(Throwable failure) {
		this.valid("failure", failure);
	}

	/**
	 * Valid handling of failure.
	 */
	protected void valid(String methodName, Throwable failure) {
		exception = failure;
		this.failure(methodName, (ex) -> {
			assertSame("Incorrect failure", failure, ex);
		}, (builder) -> {
			builder.setNextArgumentType(Object[].class);
			builder.addEscalationType(Throwable.class);
		});
	}
}
