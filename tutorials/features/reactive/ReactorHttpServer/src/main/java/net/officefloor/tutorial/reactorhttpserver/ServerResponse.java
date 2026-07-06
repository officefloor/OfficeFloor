package net.officefloor.tutorial.reactorhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {
	private String message;
}
// END SNIPPET: tutorial