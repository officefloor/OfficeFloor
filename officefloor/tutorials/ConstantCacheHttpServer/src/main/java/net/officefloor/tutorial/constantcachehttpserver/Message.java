package net.officefloor.tutorial.constantcachehttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Message.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Data
@AllArgsConstructor
public class Message {
	private String text;
}
// END SNIPPET: tutorial