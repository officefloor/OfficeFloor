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
