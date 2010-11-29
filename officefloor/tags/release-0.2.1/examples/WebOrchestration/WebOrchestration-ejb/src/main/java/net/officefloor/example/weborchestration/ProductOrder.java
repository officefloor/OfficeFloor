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

import javax.persistence.Entity;

/**
 * Order for {@link Product}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProductOrder {

	/**
	 * {@link Product} to order.
	 */
	private Product product;

	/**
	 * Quantity to order.
	 */
	private int quantity;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public ProductOrder() {
	}

	/**
	 * Initiate.
	 * 
	 * @param product
	 *            {@link Product} to order.
	 * @param quantity
	 *            Quantity to order.
	 */
	public ProductOrder(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	/**
	 * Obtains the {@link Product} to order.
	 * 
	 * @return {@link Product} to order.
	 */
	public Product getProduct() {
		return this.product;
	}

	/**
	 * Obtains the quantity of the {@link Product} to order.
	 * 
	 * @return Quantity of the {@link Product} to order.
	 */
	public int getQuantity() {
		return this.quantity;
	}

}