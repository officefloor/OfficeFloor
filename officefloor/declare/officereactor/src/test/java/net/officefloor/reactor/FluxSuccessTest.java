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

import static org.junit.Assert.assertArrayEquals;

import reactor.core.publisher.Flux;

/**
 * Tests success with {@link Flux}.
 * 
 * @author Daniel Sagenschneider
 */
public class FluxSuccessTest extends AbstractReactorTestCase {

	private static final Object object = new Object();

	/**
	 * Ensure single {@link Object}.
	 */
	public void testSingleObject() {
		this.valid("Object", Object[].class, object);
	}

	public Flux<Object> successObject() {
		return Flux.just(object);
	}

	/**
	 * Ensure multiple {@link Object}.
	 */
	public void testObjects() {
		this.valid("Objects", Object[].class, object, object, object);
	}

	public Flux<Object> successObjects() {
		return Flux.just(object, object, object);
	}

	/**
	 * Ensure {@link Void}.
	 */
	public void testVoid() {
		this.valid("Void", (Class<?>) null, (Object[]) null);
	}

	public Flux<Void> successVoid() {
		return Flux.empty();
	}

	/**
	 * Ensure single {@link String}.
	 */
	public void testString() {
		this.valid("String", String[].class, "TEST");
	}

	public Flux<String> successString() {
		return Flux.just("TEST");
	}

	/**
	 * Ensure multiple {@link String} instances.
	 */
	public void testStrings() {
		this.valid("Strings", String[].class, "ONE", "TWO", "THREE");
	}

	public Flux<String> successStrings() {
		return Flux.just("ONE", "TWO", "THREE");
	}

	/**
	 * Ensure raw.
	 */
	public void testRaw() {
		this.valid("Raw", Object[].class, object, "RAW", object);
	}

	@SuppressWarnings("rawtypes")
	public Flux successRaw() {
		return Flux.just(object, "RAW", object);
	}

	/**
	 * Ensure wildcard.
	 */
	public void testWildcard() {
		this.valid("Wildcard", Object[].class, object, "WILDCARD", object);
	}

	public Flux<?> successWildcard() {
		return Flux.just(object, "WILDCARD", object);
	}

	/**
	 * Ensure <code>null</code> {@link Flux}.
	 */
	public void testNullFlux() {
		this.valid("NullFlux", Integer[].class, (Object[]) null);
	}

	public Flux<Integer> successNullFlux() {
		return null;
	}

	/**
	 * Ensure can handle mapped values.
	 */
	public void testFlatMap() {
		this.valid("FlatMap", String[].class, "*>ONE", "*>TWO", "*>THREE");
	}

	public Flux<String> successFlatMap() {
		return Flux.just("ONE", "TWO", "THREE").flatMap(value -> Flux.just("*>" + value));
	}

	/**
	 * Validate success.
	 * 
	 * @param methodSuffix        Method name suffix.
	 * @param expectedSuccessType Expected success type.
	 * @param expectedSuccess     Expected success.
	 */
	protected void valid(String methodSuffix, Class<?> expectedSuccessType, Object... expectedSuccesses) {
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
			assertArrayEquals("Incorrect success", expectedSuccesses, (Object[]) success);
		});
	}

}
