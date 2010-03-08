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
	 * Number of {@link Product} instances available.
	 */
	private int numberAvailable;

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
	 * @param numberAvailable
	 *            Number of {@link Product} instances available.
	 */
	public ProductAvailability(Product product, int numberAvailable) {
		this.productId = product.getProductId();
		this.product = product;
		this.numberAvailable = numberAvailable;
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
	 * Obtains the number available.
	 * 
	 * @return Number available.
	 */
	public int getNumberAvailable() {
		return this.numberAvailable;
	}

	/**
	 * Specifies the number available.
	 * 
	 * @param numberAvailable
	 *            Number available.
	 */
	public void setNumberAvailable(int numberAvailable) {
		this.numberAvailable = numberAvailable;
	}

}