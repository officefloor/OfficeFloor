package net.officefloor.tutorial.springrestopenapi;

import io.swagger.v3.oas.annotations.Operation;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class GetProductService {

	@Operation(summary = "Get product by ID")
	public void service(@PathVariable(name = "id") Long id,
			ProductCatalogueService catalogue,
			ObjectResponse<Product> response) {
		response.send(catalogue.findById(id));
	}
}
// END SNIPPET: tutorial
