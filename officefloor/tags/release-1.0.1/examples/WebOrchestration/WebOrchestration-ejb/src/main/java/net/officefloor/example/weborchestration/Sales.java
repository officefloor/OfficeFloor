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

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Sales {@link Stateless} bean.
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
		 * Name.
		 */
		public final String name;

		/**
		 * Initiate.
		 * 
		 * @param email
		 *            Email.
		 * @param name
		 *            Name.
		 */
		public CustomerSeed(String email, String name) {
			this.email = email;
			this.name = name;
		}
	}

	/**
	 * Seed to create the {@link ShoppingCart}.
	 */
	public static class ShoppingCartSeed {

		/**
		 * {@link Customer}.
		 */
		public final Customer customer;

		/**
		 * Initiate.
		 * 
		 * @param customer
		 *            {@link Customer}.
		 */
		public ShoppingCartSeed(Customer customer) {
			this.customer = customer;
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
		Customer customer = new Customer(new CustomerSeed(email, name));

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
	public ShoppingCart retrieveShoppingCart(Customer customer) {
		// Obtain shopping cart for customer
		Query query = this.entityManager
				.createQuery("SELECT s FROM ShoppingCart s WHERE s.customer = :customer");
		query.setParameter("customer", customer);
		List<ShoppingCart> shoppingCarts = this.retrieveShoppingCarts(query);

		// Use existing shopping cart
		if (shoppingCarts.size() > 0) {
			return shoppingCarts.get(0);
		}

		// No shopping cart, so create and return
		ShoppingCart shoppingCart = new ShoppingCart(new ShoppingCartSeed(
				customer));
		this.entityManager.persist(shoppingCart);
		return shoppingCart;
	}

	@Override
	public void storeShoppingCart(ShoppingCart shoppingCart) {
		// Merge in changes
		this.entityManager.merge(shoppingCart);
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

	/**
	 * Obtains the {@link ShoppingCart} instances for the {@link Query}.
	 * 
	 * @param query
	 *            {@link Query}.
	 * @return {@link ShoppingCart} instances.
	 */
	@SuppressWarnings("unchecked")
	private List<ShoppingCart> retrieveShoppingCarts(Query query) {
		return (List<ShoppingCart>) query.getResultList();
	}

}