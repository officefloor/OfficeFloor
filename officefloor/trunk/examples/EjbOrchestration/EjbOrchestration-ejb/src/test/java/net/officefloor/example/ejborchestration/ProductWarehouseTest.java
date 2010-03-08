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

/**
 * Tests the {@link ProductWarehouse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProductWarehouseTest extends EjbTestCase {

	/**
	 * {@link ProductWarehouse} to test.
	 */
	private ProductWarehouseLocal warehouse = this
			.lookup(ProductWarehouseLocal.class);

	/**
	 * {@link ProductCatalog}.
	 */
	private ProductCatalogLocal catalog = this
			.lookup(ProductCatalogLocal.class);

	/**
	 * Test able to obtain zero availability on no entry.
	 */
	public void testNoAvailabilityEntry() {

		// Create the product
		Product product = new Product("Test");
		this.catalog.createProduct(product);

		// Should have no availability for product
		ProductAvailability availability = this.warehouse
				.retrieveProductAvailability(product);
		assertEquals("Incorrect product", product, availability.getProduct());
		assertEquals("Incorrect number available", 0, availability
				.getNumberAvailable());

		// Attempt to obtain again
		ProductAvailability another = this.warehouse
				.retrieveProductAvailability(product);
		assertNotSame("Should be another instance", availability, another);
		assertEquals("Should still be zero", 0, another.getNumberAvailable());
	}

	/**
	 * Test able to obtain availability.
	 */
	public void testAvailability() {

		// Create the product availability
		Product product = new Product("Test");
		this.catalog.createProduct(product);
		this.warehouse.incrementProductAvailability(product, 1);

		// Ensure have an available product
		ProductAvailability availability = this.warehouse
				.retrieveProductAvailability(product);
		assertEquals("Should have a product available", 1, availability
				.getNumberAvailable());

		// Ensure persisted
		ProductAvailability another = this.warehouse
				.retrieveProductAvailability(product);
		assertNotSame("Should be another instance", availability, another);
		assertEquals("Should still be available", 1, another
				.getNumberAvailable());
	}

	/**
	 * Allocates {@link Product}
	 */
	public void testAllocateProduct() {
		fail("TODO test once have PurchaseOrderLineItem working");
	}

}