package net.officefloor.tutorial.springrestsupplier;

import java.util.concurrent.LinkedBlockingDeque;

// START SNIPPET: tutorial
/**
 * Publishes messages to the shared in-memory queue provided by
 * {@link MessagingSupplierSource}.
 *
 * <p>Instances are created by the supplier on each request (PROCESS scope).
 * They all write to the same {@link LinkedBlockingDeque} that the supplier
 * holds for the lifetime of the application.
 */
public class MessagePublisher {

	private final LinkedBlockingDeque<String> queue;

	MessagePublisher(LinkedBlockingDeque<String> queue) {
		this.queue = queue;
	}

	public void publish(String content) {
		queue.addLast(content);
	}
}
// END SNIPPET: tutorial
