package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class AdminReportService {

	public void service(ObjectResponse<String> response) {
		response.send("Admin Report");
	}
}
// END SNIPPET: tutorial
