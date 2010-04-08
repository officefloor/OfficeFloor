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
package net.officefloor.example.weborchestration;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * {@link Product} warehouse {@link Stateless} bean.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class ProductWarehouse implements ProductWarehouseLocal {

	/**
	 * {@link EntityManager}.
	 */
	@PersistenceContext(unitName = "product-unit")
	private EntityManager entityManager;

	/*
	 * ==================== ProductWarehouseLocal ==========================
	 */

	@Override
	public ProductAvailability retrieveProductAvailability(Product product) {

		// Obtain the availability
		ProductAvailability availability = this.entityManager.find(
				ProductAvailability.class, product.getProductId());
		if (availability == null) {
			// Default the availability to zero
			availability = new ProductAvailability(product, 0);
			this.entityManager.persist(availability);
		}

		// Return the availability
		return availability;
	}

	@Override
	public List<ProductOrder> allocateProduct(Invoice invoice) {

		// Create list of potential product orders
		List<ProductOrder> orders = new LinkedList<ProductOrder>();

		// Allocate products
		for (InvoiceLineItem lineItem : invoice.getInvoiceLineItems()) {

			// Obtain quantity required
			int quantityRequired = lineItem.getQuantity();

			// Determine line item allocation
			ProductAllocation allocation = lineItem.getProductAllocation();
			if (allocation == null) {
				// Ensure have allocation
				allocation = new ProductAllocation(0);
				lineItem.setProductAllocation(allocation);
			}
			int quantityAllocated = allocation.getQuantityAllocated();

			// Determine difference in quantity
			int quantityDifference = quantityRequired - quantityAllocated;
			if (quantityDifference == 0) {
				continue; // line item allocated
			}

			// Obtain the quantity availability
			Product product = lineItem.getProduct();
			ProductAvailability availability = this
					.retrieveProductAvailability(product);
			int quantityAvailable = availability.getQuantityAvailable();

			// Determine amount to allocate
			int quantityToAllocate = Math.min(quantityDifference,
					quantityAvailable);

			// Determine if require to order more product
			if (quantityToAllocate < quantityDifference) {
				// Order more product
				int quantityToOrder = quantityDifference - quantityToAllocate;
				orders.add(new ProductOrder(product, quantityToOrder));

				// Flag the Invoice has outstanding product allocation
				OutstandingProductAllocation outstanding = new OutstandingProductAllocation(
						product, invoice);
				this.entityManager.persist(outstanding);
			}

			// Allocate the product
			allocation.setQuantityAllocated(allocation.getQuantityAllocated()
					+ quantityToAllocate);
			availability.setQuantityAvailable(availability
					.getQuantityAvailable()
					- quantityToAllocate);
		}

		// Store changes to Invoice
		this.entityManager.merge(invoice);

		// Return the orders
		return (orders.size() == 0 ? null : orders);
	}

	@Override
	public List<Invoice> productDelivered(Product product, int quantityDelivered) {

		// Retrieve the Product Availability
		ProductAvailability availability = this
				.retrieveProductAvailability(product);

		// Increment the availability
		availability.setQuantityAvailable(availability.getQuantityAvailable()
				+ quantityDelivered);

		// Obtain outstanding allocations for the product
		Query query = this.entityManager
				.createQuery("SELECT o FROM OutstandingProductAllocation o WHERE o.product = :product");
		query.setParameter("product", product);
		List<OutstandingProductAllocation> outstandingAllocations = this
				.retrieveOutstandingProductAllocations(query);

		// Iterate over outstanding invoices to full fill
		List<Invoice> fullFilledInvoices = new LinkedList<Invoice>();
		for (OutstandingProductAllocation outstanding : outstandingAllocations) {

			// Obtain the Invoice
			Invoice invoice = outstanding.getInvoice();

			// Allocate the available products
			for (InvoiceLineItem lineItem : invoice.getInvoiceLineItems()) {
				if (product.getProductId().equals(
						lineItem.getProduct().getProductId())) {

					// Determine outstanding quantity
					ProductAllocation currentAllocation = lineItem
							.getProductAllocation();
					int outstandingQuantity = (lineItem.getQuantity() - currentAllocation
							.getQuantityAllocated());

					// Determine additional allocation
					int additionalAllocation = Math.min(availability
							.getQuantityAvailable(), outstandingQuantity);

					// Allocate the additional quantity
					currentAllocation.setQuantityAllocated(currentAllocation
							.getQuantityAllocated()
							+ additionalAllocation);
					availability.setQuantityAvailable(availability
							.getQuantityAvailable()
							- additionalAllocation);

					// No longer outstanding if full filled
					if (additionalAllocation == outstandingQuantity) {
						this.entityManager.remove(outstanding);
					}
				}
			}

			// Determine if invoice is full filled
			boolean isFullFilled = true;
			for (InvoiceLineItem lineItem : invoice.getInvoiceLineItems()) {

				// Determine outstanding quantity
				ProductAllocation currentAllocation = lineItem
						.getProductAllocation();
				int outstandingQuantity = (lineItem.getQuantity() - currentAllocation
						.getQuantityAllocated());

				// Not full filled if still have outstanding quantity
				if (outstandingQuantity > 0) {
					isFullFilled = false;
				}
			}
			if (isFullFilled) {
				// Full filled the Invoices
				fullFilledInvoices.add(invoice);
			}
		}

		// Return the full filled Invoices
		return fullFilledInvoices;
	}

	/**
	 * Obtains the listing of {@link OutstandingProductAllocation}.
	 * 
	 * @param query
	 *            {@link Query}.
	 * @return listing of {@link OutstandingProductAllocation}.
	 */
	@SuppressWarnings("unchecked")
	private List<OutstandingProductAllocation> retrieveOutstandingProductAllocations(
			Query query) {
		return (List<OutstandingProductAllocation>) query.getResultList();
	}

}