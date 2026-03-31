package net.officefloor.tutorial.springresthttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GreetingRequest {

	private String name;
}
// END SNIPPET: tutorial
