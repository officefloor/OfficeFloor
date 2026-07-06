package net.officefloor.tutorial.loggerhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

// START SNIPPET: tutorial
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedRequest {
	private String message;
}
// END SNIPPET: tutorial