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

import javax.ejb.Local;

/**
 * {@link Local} interface for {@link Sales}.
 * 
 * @author Daniel Sagenschneider
 */
@Local
public interface SalesLocal {

	/**
	 * Creates a {@link Customer}.
	 * 
	 * @param email
	 *            Email address of {@link Customer}.
	 * @param name
	 *            Name of {@link Customer}.
	 * @return Created {@link Customer}.
	 * @throws CustomerExistsException
	 *             Should the {@link Customer} already exist.
	 */
	Customer createCustomer(String email, String name)
			throws CustomerExistsException;

	/**
	 * Retrieves the {@link Customer}.
	 * 
	 * @param email
	 *            Email address of the {@link Customer}.
	 * @return {@link Customer} or <code>null</code> if no {@link Customer}.
	 */
	Customer retrieveCustomer(String email);

	/**
	 * Retrieves the {@link ShoppingCart} for a {@link Customer}.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 * @return {@link ShoppingCart}.
	 */
	ShoppingCart retrieveShoppingCart(Customer customer);

	/**
	 * Stores the {@link ShoppingCart}.
	 * 
	 * @param shoppingCart
	 *            {@link ShoppingCart}.
	 */
	void storeShoppingCart(ShoppingCart shoppingCart);

}