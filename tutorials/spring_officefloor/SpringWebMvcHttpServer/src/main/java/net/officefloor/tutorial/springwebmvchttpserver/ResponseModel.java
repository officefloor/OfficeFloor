package net.officefloor.tutorial.springwebmvchttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseModel {

	private String message;
}
// END SNIPPET: tutorial