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
