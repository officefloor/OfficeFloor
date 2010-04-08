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
import javax.persistence.ManyToOne;

/**
 * Outstanding {@link ProductAllocation}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class OutstandingProductAllocation {

	/**
	 * {@link OutstandingProductAllocation} identifier.
	 */
	@Id
	@GeneratedValue
	@SuppressWarnings("unused")
	private Long outstandingProductAllocationId;

	/**
	 * {@link Product}.
	 */
	@ManyToOne
	private Product product;

	/**
	 * {@link Invoice} with outstanding {@link Product} to be allocated.
	 */
	@ManyToOne
	private Invoice invoice;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public OutstandingProductAllocation() {
	}

	/**
	 * Initiate.
	 * 
	 * @param product
	 *            {@link Product}.
	 * @param invoice
	 *            {@link Invoice}.
	 */
	public OutstandingProductAllocation(Product product, Invoice invoice) {
		this.product = product;
		this.invoice = invoice;
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
	 * Obtains the {@link Invoice}.
	 * 
	 * @return {@link Invoice}.
	 */
	public Invoice getInvoice() {
		return this.invoice;
	}

}