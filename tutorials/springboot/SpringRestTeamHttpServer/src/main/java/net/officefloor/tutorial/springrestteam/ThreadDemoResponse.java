package net.officefloor.tutorial.springrestteam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDemoResponse {

	private String socketThread;
	private String databaseThread;
	private int tableCount;
}
// END SNIPPET: tutorial
