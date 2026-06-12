package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class YamlAuthorizeService {

	public void service(ObjectResponse<String> response) {
		response.send("Admin via YAML");
	}
}
// END SNIPPET: tutorial
