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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * {@link Invoice} line item.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class InvoiceLineItem {

	/**
	 * {@link InvoiceLineItem} identifier.
	 */
	@Id
	@GeneratedValue
	@SuppressWarnings("unused")
	private Long lineItemId;

	/**
	 * {@link Product}.
	 */
	@ManyToOne
	private Product product;

	/**
	 * Price invoiced for the {@link Product}.
	 */
	private double productPrice;

	/**
	 * Quantity of {@link Product} invoiced.
	 */
	private int quantity;

	/**
	 * Price invoiced for this {@link InvoiceLineItem}.
	 */
	private double lineItemPrice;

	/**
	 * {@link ProductAllocation}.
	 */
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private ProductAllocation allocation;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public InvoiceLineItem() {
	}

	/**
	 * Initiate.
	 * 
	 * @param product
	 *            {@link Product}.
	 * @param productPrice
	 *            Price invoiced for the {@link Product}.
	 * @param quantity
	 *            Quantity of {@link Product} invoiced.
	 * @param lineItemPrice
	 *            Price invoiced for this {@link InvoiceLineItem}.
	 */
	public InvoiceLineItem(Product product, double productPrice, int quantity,
			double lineItemPrice) {
		this.product = product;
		this.productPrice = productPrice;
		this.quantity = quantity;
		this.lineItemPrice = lineItemPrice;
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
	 * Obtains the price for a single {@link Product}.
	 * 
	 * @return Price for a single {@link Product}.
	 */
	public double getProductPrice() {
		return this.productPrice;
	}

	/**
	 * Obtains the quantity of the {@link Product}.
	 * 
	 * @return Quantity of the {@link Product}.
	 */
	public int getQuantity() {
		return this.quantity;
	}

	/**
	 * Obtains the price for this {@link InvoiceLineItem}.
	 * 
	 * @return Price for this {@link InvoiceLineItem}.
	 */
	public double getInvoiceLineItemPrice() {
		return this.lineItemPrice;
	}

	/**
	 * Obtains the {@link ProductAllocation}.
	 * 
	 * @return {@link ProductAllocation}.
	 */
	public ProductAllocation getProductAllocation() {
		return this.allocation;
	}

	/**
	 * Specifies the {@link ProductAllocation}.
	 * 
	 * @param allocation
	 *            {@link ProductAllocation}.
	 */
	void setProductAllocation(ProductAllocation allocation) {
		this.allocation = allocation;
	}

}