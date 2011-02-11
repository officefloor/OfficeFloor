/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.example.weborchestration;

import java.util.Arrays;
import java.util.List;

import net.officefloor.example.weborchestration.Accounts;
import net.officefloor.example.weborchestration.AccountsLocal;
import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Invoice;
import net.officefloor.example.weborchestration.InvoiceLineItem;
import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductAllocation;
import net.officefloor.example.weborchestration.ProductAvailability;
import net.officefloor.example.weborchestration.ProductCatalog;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.ProductOrder;
import net.officefloor.example.weborchestration.ProductWarehouse;
import net.officefloor.example.weborchestration.ProductWarehouseLocal;
import net.officefloor.example.weborchestration.Quote;
import net.officefloor.example.weborchestration.Sales;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCartItem;

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
	 * {@link Sales}.
	 */
	private SalesLocal sales = this.lookup(SalesLocal.class);

	/**
	 * {@link Accounts}.
	 */
	private AccountsLocal accounts = this.lookup(AccountsLocal.class);

	/**
	 * Test able to obtain zero availability on no entry.
	 */
	public void testNoAvailabilityEntry() {

		// Create the product
		Product product = this.catalog.createProduct("Test", 1.00);

		// Create the availability
		ProductAvailability createdAvailability = this.warehouse
				.retrieveProductAvailability(product);
		this.validateProductAvailability(createdAvailability, product, 0);

		// Retrieve the availability
		ProductAvailability retrievedAvailability = this.warehouse
				.retrieveProductAvailability(product);
		this.validateProductAvailability(retrievedAvailability, product, 0);
		assertNotSame("Should be another instance", createdAvailability,
				retrievedAvailability);
	}

	/**
	 * Validates the {@link ProductAvailability}.
	 */
	private void validateProductAvailability(ProductAvailability availability,
			Product product, int quantity) {
		assertNotNull("Must have availability", availability);
		assertEquals("Incorrect product", product.getProductId(), availability
				.getProduct().getProductId());
		assertEquals("Incorrect number available", quantity, availability
				.getQuantityAvailable());
	}

	/**
	 * Test able to obtain availability.
	 */
	public void testAvailability() {

		// Create the product availability
		Product product = this.catalog.createProduct("Test", 1.00);
		final int quantity = 1;
		this.warehouse.productDelivered(product, quantity);

		// Ensure have an available product
		ProductAvailability availability = this.warehouse
				.retrieveProductAvailability(product);
		this.validateProductAvailability(availability, product, quantity);
	}

	/**
	 * Tests allocating {@link Product}.
	 */
	public void testAllocateProduct() throws Exception {

		// Create the Product
		Product product = this.catalog.createProduct("Test", 1.00);

		// Create the Invoice
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");
		final int quantity = 1;
		Quote quote = this.accounts.createQuote(customer, Arrays
				.asList(new ShoppingCartItem(product, quantity)));
		Invoice invoice = this.accounts.createInvoice(quote);

		// Deliver product to warehouse
		this.warehouse.productDelivered(product, 10);

		// Allocate product to Invoice
		List<ProductOrder> orders = this.warehouse.allocateProduct(invoice);
		assertNull("All product should be available so no order required",
				orders);

		// Retrieve invoice to ensure product allocated
		invoice = this.accounts.retrieveInvoice(invoice.getInvoiceId());
		InvoiceLineItem lineItem = invoice.getInvoiceLineItems().get(0);
		ProductAllocation allocation = lineItem.getProductAllocation();
		assertNotNull("Must have allocation", allocation);
		assertEquals("Incorrect quantity allocated", 1, allocation
				.getQuantityAllocated());

		// Ensure available products decremented
		ProductAvailability availability = this.warehouse
				.retrieveProductAvailability(product);
		assertEquals("Incorrect products available", 9, availability
				.getQuantityAvailable());
	}

	/**
	 * Tests {@link ProductOrder} is returned as {@link Product} not available.
	 */
	public void testOrderProduct() throws Exception {

		// Create the Product
		Product product = this.catalog.createProduct("Test", 1.00);

		// Create the Invoice
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");
		final int quantity = 1;
		Quote quote = this.accounts.createQuote(customer, Arrays
				.asList(new ShoppingCartItem(product, quantity)));
		Invoice invoice = this.accounts.createInvoice(quote);

		// Allocate product to Invoice
		List<ProductOrder> orders = this.warehouse.allocateProduct(invoice);
		assertNotNull("Product not available so order required", orders);
		assertEquals("Incorrect number of orders", 1, orders.size());
		ProductOrder order = orders.get(0);
		assertEquals("Incorrect product", product.getProductId(), order
				.getProduct().getProductId());
		assertEquals("Incorrect quantity", quantity, order.getQuantity());

		// Deliver the product order and ensure Invoice full filled
		List<Invoice> fullFilledInvoices = this.warehouse.productDelivered(
				product, quantity);
		assertNotNull("Should have full filled invoices", fullFilledInvoices);
		assertEquals("Should have a full filled invoice", 1, fullFilledInvoices
				.size());
		Invoice fullFilledInvoice = fullFilledInvoices.get(0);
		assertEquals("Incorrect full filled invoice", invoice.getInvoiceId(),
				fullFilledInvoice.getInvoiceId());
	}

}