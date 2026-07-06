package net.officefloor.tutorial.springrestopenapi;

import org.springframework.stereotype.Service;

import java.util.List;

// START SNIPPET: tutorial
@Service
public class ProductCatalogueService {

	private final List<Product> products = List.of(
			new Product(1L, "Widget", 999),
			new Product(2L, "Gadget", 1999),
			new Product(3L, "Doohickey", 4999));

	public List<Product> findAll() {
		return products;
	}

	public Product findById(long id) {
		return products.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
	}
}
// END SNIPPET: tutorial
