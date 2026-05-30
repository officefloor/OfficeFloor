package net.officefloor.tutorial.springwebfluxhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Response model.
 * 
 * @author Daniel Sagenschneider
 */
@HttpObject
// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestModel {

	private String input;
}
// END SNIPPET: tutorial