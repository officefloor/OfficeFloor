/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.example.ejborchestration;

import java.util.List;


/**
 * Tests the {@link ProductCatalog}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProductCatalogTest extends EjbTestCase {

	/**
	 * {@link ProductCatalog} to test.
	 */
	private ProductCatalogLocal catalog = this
			.lookup(ProductCatalogLocal.class);

	/**
	 * Ensure able to create and retrieve a {@link Product}.
	 */
	public void testCreateAndRetrieveProduct() {

		// Create a product
		Product createdProduct = new Product("test");
		this.catalog.createProduct(createdProduct);
		assertNotNull("Should have identifier for product", createdProduct
				.getProductId());

		// Ensure can retrieve by identifier
		Product retrievedProduct = this.catalog.retrieveProduct(createdProduct
				.getProductId());
		assertNotSame("Should be different instance retrieved", createdProduct,
				retrievedProduct);
		assertEquals("Incorrect product name", "test", retrievedProduct
				.getName());
	}

	/**
	 * Ensure able to retrieve products by name.
	 */
	public void testRetrieveProductsByName() {

		// Create a product
		Product createdProduct = new Product("test");
		this.catalog.createProduct(createdProduct);

		// Ensure can retrieve the product
		List<Product> products = this.catalog.retrieveProductsByName("test");
		assertEquals("Should only find a single product", 1, products.size());
		Product product = products.get(0);
		assertNotNull("Should add product to catalog", product);
		assertNotSame("Should be different instance retrieved", createdProduct,
				product);
		assertEquals("Incorrect product id", createdProduct.getProductId(),
				product.getProductId());
		assertEquals("Incorrect product name", "test", product.getName());
	}

	/**
	 * Ensure able to retrieve {@link Product} listing.
	 */
	public void testRetrieveProductList() {

		// Create some products
		this.catalog.createProduct(new Product("One"));
		this.catalog.createProduct(new Product("Two"));

		// Ensure can retrieve listing of products
		List<Product> products = this.catalog.retrieveProductList();
		assertEquals("Incorrect number of products", 2, products.size());
	}

}