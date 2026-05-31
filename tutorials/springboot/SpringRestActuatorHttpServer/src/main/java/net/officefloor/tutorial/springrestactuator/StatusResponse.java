package net.officefloor.tutorial.springrestactuator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {

	private String status;
}
// END SNIPPET: tutorial
