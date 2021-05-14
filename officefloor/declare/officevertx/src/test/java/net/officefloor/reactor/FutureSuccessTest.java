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