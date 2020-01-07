/*-
 * #%L
 * Reactor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
