package net.officefloor.tutorial.scratchopenapi;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
public class NotFoundExceptionHandler {

	public void handle(@Parameter NotFoundException ex,
			ObjectResponse<ResponseEntity<Void>> response) {
		response.send(ResponseEntity.notFound().build());
	}
}
// END SNIPPET: tutorial
