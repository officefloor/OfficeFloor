package net.officefloor.tutorial.springrestsupplier;

import java.util.concurrent.LinkedBlockingDeque;

// START SNIPPET: tutorial
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
