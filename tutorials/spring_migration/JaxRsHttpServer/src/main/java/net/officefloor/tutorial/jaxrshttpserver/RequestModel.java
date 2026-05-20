package net.officefloor.tutorial.jaxrshttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Request model.
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