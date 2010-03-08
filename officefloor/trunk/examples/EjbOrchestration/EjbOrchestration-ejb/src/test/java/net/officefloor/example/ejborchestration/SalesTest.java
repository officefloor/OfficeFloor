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
 * Tests {@link Sales}.
 * 
 * @author Daniel Sagenschneider
 */
public class SalesTest extends EjbTestCase {

	/**
	 * {@link Sales} to test.
	 */
	private SalesLocal sales = this.lookup(SalesLocal.class);

	/**
	 * {@link ProductCatalog} to aid testing.
	 */
	private ProductCatalogLocal catalog = this
			.lookup(ProductCatalogLocal.class);

	/**
	 * Tests create the {@link Customer}.
	 */
	public void testCreateAndRetrieveCustomer() throws Exception {

		final String EMAIL = "test@officefloor.net";
		final String NAME = "Daniel";

		// Create the customer
		Customer createdCustomer = this.sales.createCustomer(EMAIL, NAME);
		assertNotNull("Must create customer Id", createdCustomer
				.getCustomerId());
		assertEquals("Incorrect email", EMAIL, createdCustomer.getEmail());
		assertEquals("Incorrect name", NAME, createdCustomer.getName());

		// Retrieve the customer
		Customer retrievedCustomer = this.sales.retrieveCustomer(EMAIL);
		assertNotNull("Must retrieve customer", retrievedCustomer);
		assertNotSame("Should be different instance retrieved",
				createdCustomer, retrievedCustomer);
		assertEquals("Incorrect retrieved Id", createdCustomer.getCustomerId(),
				retrievedCustomer.getCustomerId());
		assertEquals("Incorrect retrieved email", EMAIL, retrievedCustomer
				.getEmail());
		assertEquals("Incorrect retrieved name", NAME, retrievedCustomer
				.getName());
	}

	/**
	 * Ensure can not create a duplicate {@link Customer}.
	 */
	public void testNotCreateDuplicateCustomer() throws Exception {

		final String EMAIL = "test@officefloor.net";

		// Create the customer
		Customer customer = this.sales.createCustomer(EMAIL, "Daniel");
		assertNotNull("Ensure create customer", customer);

		// Ensure fail on creating duplicate customer (by email)
		try {
			this.sales.createCustomer(EMAIL, "Aaron");
			fail("Should not be able to create duplicate customer");
		} catch (CustomerExistsException ex) {
			assertEquals("Incorrect cause", "Customer with email " + EMAIL
					+ " already exists", ex.getMessage());
		}
	}

	/**
	 * Ensure able to create and retrieve a {@link PurchaseOrder}.
	 */
	public void testCreateAndRetrievePurchaseOrder() throws Exception {

		// Create the Customer
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");

		// Create the Purchase Order
		PurchaseOrder createdPurchaseOrder = new PurchaseOrder(customer);
		this.sales.createPurchaseOrder(createdPurchaseOrder);
		assertNotNull("Ensure received Id", createdPurchaseOrder
				.getPurchaseOrderId());

		// Retrieve the Purchase Order
		PurchaseOrder retrievedPurchaseOrder = this.sales
				.retrievePurchaseOrder(createdPurchaseOrder
						.getPurchaseOrderId());
		assertEquals("Incorrect purchase order retrieved", createdPurchaseOrder
				.getPurchaseOrderId(), retrievedPurchaseOrder
				.getPurchaseOrderId());
		assertNotSame("Should be different instance retrieved",
				createdPurchaseOrder, retrievedPurchaseOrder);
		assertEquals("Incorrect customer", customer.getCustomerId(),
				retrievedPurchaseOrder.getCustomer().getCustomerId());
	}

	/**
	 * Ensure able to add {@link PurchaseOrderLineItem} to the
	 * {@link PurchaseOrder}.
	 */
	public void testAddLineItem() throws Exception {

		// Create the Customer
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");

		// Create the Product
		Product product = new Product("test");
		this.catalog.createProduct(product);

		final int quantity = 12;

		// Create the Purchase Order with a line item
		PurchaseOrder createdPurchaseOrder = new PurchaseOrder(customer);
		PurchaseOrderLineItem createdLineItem = new PurchaseOrderLineItem(
				product, quantity);
		createdPurchaseOrder.getPurchaseOrderLineItems().add(createdLineItem);
		this.sales.createPurchaseOrder(createdPurchaseOrder);
		assertNotNull("Ensure line item id provided", createdLineItem
				.getPurchaseOrderLineItemId());

		// Retrieve the Purchase Order with the line item
		PurchaseOrder retrievedPurchaseOrder = this.sales
				.retrievePurchaseOrder(createdPurchaseOrder
						.getPurchaseOrderId());
		assertEquals("Incorrect purchase order retrieved", createdPurchaseOrder
				.getPurchaseOrderId(), retrievedPurchaseOrder
				.getPurchaseOrderId());
		assertNotSame("Should not be same purchase order instance",
				createdPurchaseOrder, retrievedPurchaseOrder);
		assertEquals("Incorrect number of line items", 1,
				retrievedPurchaseOrder.getPurchaseOrderLineItems().size());
		PurchaseOrderLineItem retrievedLineItem = retrievedPurchaseOrder
				.getPurchaseOrderLineItems().get(0);
		assertEquals("Incorrect line item", createdLineItem
				.getPurchaseOrderLineItemId(), retrievedLineItem
				.getPurchaseOrderLineItemId());
		assertNotSame("Should not be same line item instance", createdLineItem,
				retrievedLineItem);
		assertEquals("Incorrect product", product.getProductId(),
				retrievedLineItem.getProduct().getProductId());
		assertEquals("Incorrect quantity", quantity, retrievedLineItem
				.getQuantity());
	}

}