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