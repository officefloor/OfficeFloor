package net.officefloor.tutorial.springrestcors;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.CrossOrigin;

// START SNIPPET: tutorial
@CrossOrigin(origins = "https://example.com")
public class AnnotationCorsService {

	public void service(ObjectResponse<String> response) {
		response.send("Hello from annotation CORS endpoint");
	}
}
// END SNIPPET: tutorial
