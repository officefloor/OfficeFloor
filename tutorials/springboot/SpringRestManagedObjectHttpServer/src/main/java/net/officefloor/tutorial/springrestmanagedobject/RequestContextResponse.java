package net.officefloor.tutorial.springrestmanagedobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContextResponse {

	private String correlationId;
	private long startTimeMs;
}
// END SNIPPET: tutorial
