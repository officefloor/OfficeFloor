package net.officefloor.tutorial.loggerhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Request to be logged.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedRequest {
	private String message;
}
// END SNIPPET: tutorial