package net.officefloor.tutorial.springrestsupplier;

import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
public class PublishService {

	public void service(@RequestBody Message message, MessagePublisher publisher) {
		publisher.publish(message.getContent());
	}
}
// END SNIPPET: tutorial
