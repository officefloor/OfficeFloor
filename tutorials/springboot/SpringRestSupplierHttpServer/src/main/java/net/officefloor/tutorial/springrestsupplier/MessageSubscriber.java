package net.officefloor.tutorial.springrestsupplier;

import java.util.concurrent.LinkedBlockingDeque;

// START SNIPPET: tutorial
/**
 * Receives messages from the shared in-memory queue provided by
 * {@link MessagingSupplierSource}.
 *
 * <p>Instances are created by the supplier on each request (PROCESS scope).
 * They all read from the same {@link LinkedBlockingDeque} that the supplier
 * holds for the lifetime of the application — the same queue that
 * {@link MessagePublisher} writes to.
 */
public class MessageSubscriber {

	private final LinkedBlockingDeque<String> queue;

	MessageSubscriber(LinkedBlockingDeque<String> queue) {
		this.queue = queue;
	}

	/**
	 * Returns the oldest queued message, or {@code null} if the queue is empty.
	 */
	public String receive() {
		return queue.pollFirst();
	}
}
// END SNIPPET: tutorial
