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
