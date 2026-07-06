package net.officefloor.tutorial.springrestopenapi;

import io.swagger.v3.oas.annotations.Operation;
import net.officefloor.web.ObjectResponse;

import java.util.List;

// START SNIPPET: tutorial
public class ListProductsService {

	@Operation(summary = "List all products")
	public void service(ProductCatalogueService catalogue, ObjectResponse<List<Product>> response) {
		response.send(catalogue.findAll());
	}
}
// END SNIPPET: tutorial
