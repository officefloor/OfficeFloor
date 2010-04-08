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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * {@link Product} allocation.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class ProductAllocation {

	/**
	 * {@link ShoppingCartItem} weak reference.
	 */
	@Id
	@GeneratedValue
	private Long allocationId;

	/**
	 * Quantity allocated.
	 */
	private int quantityAllocated;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public ProductAllocation() {
	}

	/**
	 * Initiate.
	 * 
	 * @param quantityAllocated
	 *            Quantity allocated.
	 */
	public ProductAllocation(int quantityAllocated) {
		this.quantityAllocated = quantityAllocated;
	}

	/**
	 * Obtains the identifier for this {@link ProductAllocation}.
	 * 
	 * @return Identifier for this {@link ProductAllocation}.
	 */
	public Long getProductAllocationId() {
		return this.allocationId;
	}

	/**
	 * Obtains the quantity allocated.
	 * 
	 * @return Quantity allocated.
	 */
	public int getQuantityAllocated() {
		return this.quantityAllocated;
	}

	/**
	 * Specifies the quantity allocated.
	 * 
	 * @param quantityAllocated
	 *            Quantity allocated.
	 */
	public void setQuantityAllocated(int quantityAllocated) {
		this.quantityAllocated = quantityAllocated;
	}

}