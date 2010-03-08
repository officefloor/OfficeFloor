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
	public String name;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public Product() {
	}

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the {@link Product}.
	 */
	public Product(String name) {
		this.name = name;
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
	 * Specifies the {@link Product} name.
	 * 
	 * @param name
	 *            {@link Product} name.
	 */
	public void setName(String name) {
		this.name = name;
	}

}