package net.officefloor.tutorial.springrestrelationship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {

	private Long id;
	private String name;
}
// END SNIPPET: tutorial
