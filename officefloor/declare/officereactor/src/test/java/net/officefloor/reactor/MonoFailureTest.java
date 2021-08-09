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

import reactor.core.publisher.Mono;

/**
 * Tests failure.
 * 
 * @author Daniel Sagenschneider
 */
public class MonoFailureTest extends AbstractReactorTestCase {

	private static Throwable exception;

	/**
	 * Ensure handle {@link Throwable}.
	 */
	public void testThrowable() {
		this.valid(new Throwable("TEST"));
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

	public Mono<Object> failure() {
		return Mono.error(exception);
	}

	/**
	 * Valid handling of failure.
	 */
	protected void valid(Throwable failure) {
		exception = failure;
		this.failure("failure", (ex) -> {
			assertSame("Incorrect failure", failure, ex);
		}, (builder) -> {
			builder.setNextArgumentType(Object.class);
			builder.addEscalationType(Throwable.class);
		});
	}
}
