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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * Product availability.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class ProductAvailability {

	/**
	 * Primary key.
	 */
	@Id
	protected Long productId;

	/**
	 * {@link Product}.
	 */
	@OneToOne
	@PrimaryKeyJoinColumn
	private Product product;

	/**
	 * Quantity of {@link Product} instances available.
	 */
	private int quantityAvailable;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public ProductAvailability() {
	}

	/**
	 * Allow for creating availability.
	 * 
	 * @param product
	 *            {@link Product}.
	 * @param quantityAvailable
	 *            Number of {@link Product} instances available.
	 */
	public ProductAvailability(Product product, int quantityAvailable) {
		this.productId = product.getProductId();
		this.product = product;
		this.quantityAvailable = quantityAvailable;
	}

	/**
	 * Obtains the {@link Product}.
	 * 
	 * @return {@link Product}.
	 */
	public Product getProduct() {
		return this.product;
	}

	/**
	 * Obtains the quantity available.
	 * 
	 * @return Quantity available.
	 */
	public int getQuantityAvailable() {
		return this.quantityAvailable;
	}

	/**
	 * Specifies the quantity available.
	 * 
	 * @param quantityAvailable
	 *            Quantity available.
	 */
	public void setQuantityAvailable(int quantityAvailable) {
		this.quantityAvailable = quantityAvailable;
	}

}