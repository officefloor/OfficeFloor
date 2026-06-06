package net.officefloor.tutorial.javascripthttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
	private int id;
	private String name;
}
// END SNIPPET: tutorial
