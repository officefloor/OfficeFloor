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

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Purchasing {@link Stateless} bean.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class Sales implements SalesLocal {

	/**
	 * Seed to create the {@link Customer}.
	 */
	public static class CustomerSeed {

		/**
		 * Email.
		 */
		public final String email;

		/**
		 * Initiate.
		 * 
		 * @param email
		 *            Email.
		 */
		public CustomerSeed(String email) {
			this.email = email;
		}
	}

	/**
	 * {@link EntityManager}.
	 */
	@PersistenceContext(unitName = "product-unit")
	private EntityManager entityManager;

	/*
	 * ==================== SalesLocal ==========================
	 */

	@Override
	public Customer createCustomer(String email, String name)
			throws CustomerExistsException {

		// Determine if customer already exists
		if (this.retrieveCustomer(email) != null) {
			throw new CustomerExistsException(email);
		}

		// Create the Customer
		CustomerSeed seed = new CustomerSeed(email);
		Customer customer = new Customer(seed);
		customer.setName(name);

		// Make available to the database
		this.entityManager.persist(customer);

		// Return the customer
		return customer;
	}

	@Override
	public Customer retrieveCustomer(String email) {
		// Obtain customer by email
		Query query = this.entityManager
				.createQuery("SELECT c FROM Customer c WHERE c.email = :email");
		query.setParameter("email", email);
		List<Customer> customers = this.retrieveCustomers(query);

		// Return based on whether found the customer
		return (customers.size() == 1 ? customers.get(0) : null);
	}

	@Override
	public void createPurchaseOrder(PurchaseOrder purchaseOrder) {
		this.entityManager.persist(purchaseOrder);
	}

	@Override
	public PurchaseOrder retrievePurchaseOrder(Long purchaseOrderId) {
		return this.entityManager.find(PurchaseOrder.class, purchaseOrderId);
	}

	/**
	 * Obtains the {@link Customer} instances for the {@link Query}.
	 * 
	 * @param query
	 *            {@link Query}.
	 * @return {@link Customer} instances.
	 */
	@SuppressWarnings("unchecked")
	private List<Customer> retrieveCustomers(Query query) {
		return (List<Customer>) query.getResultList();
	}

}