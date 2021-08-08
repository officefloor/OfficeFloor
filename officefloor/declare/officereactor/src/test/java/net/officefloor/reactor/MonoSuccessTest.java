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
 * Tests success with {@link Mono}.
 * 
 * @author Daniel Sagenschneider
 */
public class MonoSuccessTest extends AbstractReactorTestCase {

	private static final Object object = new Object();

	/**
	 * Ensure {@link Object}.
	 */
	public void testObject() {
		this.valid("Object", object);
	}

	public Mono<Object> successObject() {
		return Mono.just(object);
	}

	/**
	 * Ensure {@link Void}.
	 */
	public void testVoid() {
		this.valid("Void", null);
	}

	public Mono<Void> successVoid() {
		return Mono.empty();
	}

	/**
	 * Ensure {@link String}.
	 */
	public void testString() {
		this.valid("String", "TEST");
	}

	public Mono<String> successString() {
		return Mono.just("TEST");
	}

	/**
	 * Ensure raw.
	 */
	public void testRaw() {
		this.valid("Raw", object);
	}

	@SuppressWarnings("rawtypes")
	public Mono successRaw() {
		return Mono.just(object);
	}

	/**
	 * Ensure wildcard.
	 */
	public void testWildcard() {
		this.valid("Wildcard", object);
	}

	public Mono<?> successWildcard() {
		return Mono.just(object);
	}

	/**
	 * Ensure <code>null</code> {@link Mono}.
	 */
	public void testNullMono() {
		this.valid("NullMono", null, Integer.class);
	}

	public Mono<Integer> successNullMono() {
		return null;
	}

	/**
	 * Validate success.
	 * 
	 * @param methodSuffix    Method name suffix.
	 * @param expectedSuccess Expected success.
	 */
	protected void valid(String methodSuffix, Object expectedSuccess) {
		this.valid(methodSuffix, expectedSuccess, expectedSuccess == null ? null : expectedSuccess.getClass());
	}

	/**
	 * Validate success.
	 * 
	 * @param methodSuffix        Method name suffix.
	 * @param expectedSuccess     Expected success.
	 * @param expectedSuccessType Expected success type.
	 */
	protected void valid(String methodSuffix, Object expectedSuccess, Class<?> expectedSuccessType) {
		this.test("success" + methodSuffix, (builder) -> {
			if (expectedSuccessType != null) {
				builder.setNextArgumentType(expectedSuccessType);
			}
			builder.addEscalationType(Throwable.class);
		}, (failure, success) -> {
			assertNull(
					"Should be no failure: "
							+ (failure == null ? "" : failure.getMessage() + " (" + failure.getClass().getName() + ")"),
					failure);
			assertEquals("Incorrect success", expectedSuccess, success);
		});
	}

}
