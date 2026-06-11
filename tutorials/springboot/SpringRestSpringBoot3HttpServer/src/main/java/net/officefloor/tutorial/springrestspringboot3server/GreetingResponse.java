package net.officefloor.tutorial.springrestspringboot3server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GreetingResponse {

	private String message;
}
// END SNIPPET: tutorial
