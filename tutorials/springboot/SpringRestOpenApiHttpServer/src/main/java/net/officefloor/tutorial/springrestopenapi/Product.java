package net.officefloor.tutorial.springrestopenapi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A product in the catalogue")
public class Product {

	@Schema(description = "Unique product identifier")
	private Long id;

	@Schema(description = "Product name")
	private String name;

	@Schema(description = "Product price in cents")
	private int priceCents;
}
// END SNIPPET: tutorial
