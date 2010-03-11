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
	 * Ensure able to create and retrieve a {@link ShoppingCart}.
	 */
	public void testCreateAndRetrieveShoppingCart() throws Exception {

		// Create the Customer
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");

		// Retrieve the Shopping Cart
		ShoppingCart createdShoppingCart = this.sales
				.retrieveShoppingCart(customer);
		assertNotNull("Ensure have Id", createdShoppingCart.getShoppingCartId());

		// Retrieve the Shopping Cart again
		ShoppingCart retrievedShoppingCart = this.sales
				.retrieveShoppingCart(customer);
		assertEquals("Incorrect shopping cart retrieved", createdShoppingCart
				.getShoppingCartId(), retrievedShoppingCart.getShoppingCartId());
		assertNotSame("Should be different instance retrieved",
				createdShoppingCart, retrievedShoppingCart);
		assertEquals("Incorrect customer", customer.getCustomerId(),
				retrievedShoppingCart.getCustomer().getCustomerId());
	}

	/**
	 * Ensure able to add {@link ShoppingCartItem} to the {@link ShoppingCart}.
	 */
	public void testAddItem() throws Exception {

		// Create the Customer
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");

		// Create the Product
		Product product = this.catalog.createProduct("test", 1.00);

		final int quantity = 12;

		// Create the Shopping Cart with one item
		ShoppingCart createdShoppingCart = this.sales
				.retrieveShoppingCart(customer);
		ShoppingCartItem createdItem = new ShoppingCartItem(product, quantity);
		createdShoppingCart.getShoppingCartItems().add(createdItem);

		// Store changes to the Shopping Cart
		this.sales.storeShoppingCart(createdShoppingCart);

		// Retrieve the Shopping Cart with one item
		ShoppingCart retrievedShoppingCart = this.sales
				.retrieveShoppingCart(customer);
		assertEquals("Incorrect shopping cart retrieved", createdShoppingCart
				.getShoppingCartId(), retrievedShoppingCart.getShoppingCartId());
		assertNotSame("Should not be same shopping cart instance",
				createdShoppingCart, retrievedShoppingCart);

		// Validate item added to Shopping Cart
		List<ShoppingCartItem> items = retrievedShoppingCart
				.getShoppingCartItems();
		assertEquals("Incorrect number of line items", 1, items.size());
		ShoppingCartItem retrievedItem = items.get(0);
		assertNotNull("Should have item", retrievedItem);
		assertNotSame("Should not be same item instance", createdItem,
				retrievedItem);
		assertEquals("Incorrect product", createdItem.getProduct()
				.getProductId(), retrievedItem.getProduct().getProductId());
		assertEquals("Incorrect quantity", quantity, retrievedItem
				.getQuantity());
	}

}