/*-
 * #%L
 * Vertx
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * Tests success with {@link Vertx} {@link Future}.
 * 
 * @author Daniel Sagenschneider
 */
public class FutureSuccessTest extends AbstractVertxTestCase {

	private static final Object object = new Object();

	/**
	 * Ensure {@link Object}.
	 */
	@Test
	public void object() {
		this.valid("Object", object);
	}

	public Future<Object> successObject() {
		return Future.succeededFuture(object);
	}

	/**
	 * Ensure {@link Void}.
	 */
	@Test
	public void voidValue() {
		this.valid("Void", null);
	}

	public Future<Void> successVoid() {
		return Future.succeededFuture();
	}

	/**
	 * Ensure {@link String}.
	 */
	@Test
	public void string() {
		this.valid("String", "TEST");
	}

	public Future<String> successString() {
		return Future.succeededFuture("TEST");
	}

	/**
	 * Ensure raw.
	 */
	@Test
	public void raw() {
		this.valid("Raw", object);
	}

	@SuppressWarnings("rawtypes")
	public Future successRaw() {
		return Future.succeededFuture(object);
	}

	/**
	 * Ensure wildcard.
	 */
	@Test
	public void wildcard() {
		this.valid("Wildcard", object);
	}

	public Future<?> successWildcard() {
		return Future.succeededFuture(object);
	}

	/**
	 * Ensure <code>null</code> {@link Mono}.
	 */
	@Test
	public void nullFuture() {
		this.valid("Null", null, Integer.class);
	}

	public Future<Integer> successNull() {
		return null;
	}

	/**
	 * Ensure can handle mapped values.
	 */
	@Test
	public void flatMap() {
		this.valid("FlatMap", "*>MAP");
	}

	public Future<String> successFlatMap() {
		return Future.succeededFuture("MAP").flatMap(value -> Future.succeededFuture("*>" + value));
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
			assertNull(failure, "Should be no failure: "
					+ (failure == null ? "" : failure.getMessage() + " (" + failure.getClass().getName() + ")"));
			assertEquals(expectedSuccess, success, "Incorrect success");
		});
	}

}
