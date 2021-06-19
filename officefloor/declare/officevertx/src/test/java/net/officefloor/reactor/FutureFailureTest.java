/*-
 * #%L
 * Vertx
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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import io.vertx.core.Future;
import net.officefloor.vertx.OfficeFloorVertxException;

/**
 * Tests failure.
 * 
 * @author Daniel Sagenschneider
 */
public class FutureFailureTest extends AbstractVertxTestCase {

	private static Throwable exception;

	/**
	 * Ensure handle {@link Throwable}.
	 */
	@Test
	public void throwable() {
		this.valid(new OfficeFloorVertxException(new Throwable("TEST")));
	}

	/**
	 * Ensure handle {@link Exception}.
	 */
	@Test
	public void exception() {
		this.valid(new Exception("TEST"));
	}

	/**
	 * Ensure handle {@link RuntimeException}.
	 */
	@Test
	public void runtimeException() {
		this.valid(new RuntimeException("TEST"));
	}

	/**
	 * Ensure handle {@link Error}.
	 */
	@Test
	public void error() {
		this.valid(new Error("TEST"));
	}

	public Future<Object> failure() {
		return Future.failedFuture(exception);
	}

	/**
	 * Valid handling of failure.
	 */
	protected void valid(Throwable failure) {
		exception = failure;
		this.failure("failure", (ex) -> {
			assertSame(failure, ex, "Incorrect failure");
		}, (builder) -> {
			builder.setNextArgumentType(Object.class);
			builder.addEscalationType(Throwable.class);
		});
	}

}
