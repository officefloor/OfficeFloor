package net.officefloor.tutorial.jwtresourcehttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

// START SNIPPET: tutorial
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Claims {
	private String id;
	private String[] roles;
}
// END SNIPPET: tutorial