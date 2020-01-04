/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2020 Daniel Sagenschneider
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
package net.officefloor.reactor;

import reactor.core.publisher.Mono;

/**
 * Tests success.
 * 
 * @author Daniel Sagenschneider
 */
public class SuccessTest extends AbstractReactorTestCase {

	/**
	 * Ensure {@link Object}.
	 */
	public void testObject() {
		this.success("Object", this.object);
	}

	private final Object object = new Object();

	public Mono<Object> successObject() {
		return Mono.just(this.object);
	}

	/**
	 * Ensure {@link Void}.
	 */
	public void testVoid() {
		this.success("Void", null);
	}

	public Mono<Void> successVoid() {
		return Mono.just(null);
	}

	/**
	 * Ensure {@link String}.
	 */
	public void testString() {
		this.success("String", "TEST");
	}

	public Mono<String> successString() {
		return Mono.just("TEST");
	}

	/**
	 * Ensure raw.
	 */
	public void testRaw() {
		this.success("Raw", this.object);
	}

	@SuppressWarnings("rawtypes")
	public Mono successRaw() {
		return Mono.just(this.object);
	}
	
	/**
	 * Ensure wildcard.
	 */
	public void testWildcard() {
		this.success("Wildcard", this.object);
	}
	
	public Mono<?> successWildcard() {
		return Mono.just(this.object);
	}

	/**
	 * Validate success.
	 * 
	 * @param methodSuffix    Method name suffix.
	 * @param expectedSuccess Expected success.
	 */
	protected void success(String methodSuffix, Object expectedSuccess) {
		this.success("success" + methodSuffix, expectedSuccess, (builder) -> {
			if (expectedSuccess != null) {
				builder.setNextArgumentType(expectedSuccess.getClass());
			}
		});
	}
}