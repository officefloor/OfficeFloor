package net.officefloor.tutorial.scratchopenapi;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
public class RespondWithNoContent {

	public void service(ObjectResponse<ResponseEntity<Void>> response) {
		response.send(ResponseEntity.noContent().build());
	}
}
// END SNIPPET: tutorial
