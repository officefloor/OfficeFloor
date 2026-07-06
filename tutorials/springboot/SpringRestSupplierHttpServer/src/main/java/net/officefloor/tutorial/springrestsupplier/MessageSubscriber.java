package net.officefloor.tutorial.springrestsupplier;

import java.util.concurrent.LinkedBlockingDeque;

// START SNIPPET: tutorial
public class MessageSubscriber {

	private final LinkedBlockingDeque<String> queue;

	MessageSubscriber(LinkedBlockingDeque<String> queue) {
		this.queue = queue;
	}

	public String receive() {
		return queue.pollFirst();
	}
}
// END SNIPPET: tutorial
