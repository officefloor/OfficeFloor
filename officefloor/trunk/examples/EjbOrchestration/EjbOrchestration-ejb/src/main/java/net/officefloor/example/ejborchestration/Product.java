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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import net.officefloor.example.ejborchestration.ProductCatalog.ProductSeed;

/**
 * Product.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class Product {

	/**
	 * Identifier for this {@link Product}.
	 */
	@Id
	@GeneratedValue
	private Long productId;

	/**
	 * Name of the {@link Product}.
	 */
	private String name;

	/**
	 * Price of the {@link Product}.
	 */
	private double price;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public Product() {
	}

	/**
	 * Initiate.
	 * 
	 * @param seed
	 *            {@link ProductSeed}.
	 */
	public Product(ProductSeed seed) {
		this.name = seed.name;
		this.price = seed.price;
	}

	/**
	 * Obtains the {@link Product} identifier.
	 * 
	 * @return {@link Product} identifier.
	 */
	public Long getProductId() {
		return this.productId;
	}

	/**
	 * Obtains the {@link Product} name.
	 * 
	 * @return {@link Product} name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the price for a single {@link Product}.
	 * 
	 * @return Price for a single {@link Product}.
	 */
	public double getPrice() {
		return this.price;
	}

}