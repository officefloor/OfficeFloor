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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Item within a {@link Quote}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class QuoteItem {

	/**
	 * {@link QuoteItem} identifier.
	 */
	@Id
	@GeneratedValue
	private Long quoteItemId;

	/**
	 * {@link Product}.
	 */
	@ManyToOne
	private Product product;

	/**
	 * Quoted price for a single {@link Product}.
	 */
	private double productPrice;

	/**
	 * Quantity of the {@link Product}.
	 */
	private int quantity;

	/**
	 * Price for this {@link QuoteItem}.
	 */
	private double quoteItemPrice;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public QuoteItem() {
	}

	/**
	 * Initiate.
	 * 
	 * @param product
	 *            {@link Product}.
	 * @param productPrice
	 *            Quoted price for a single {@link Product}.
	 * @param quantity
	 *            Quantity of the {@link Product}.
	 * @param quoteItemPrice
	 *            Price for this {@link QuoteItem}.
	 */
	public QuoteItem(Product product, double productPrice, int quantity,
			double quoteItemPrice) {
		this.product = product;
		this.productPrice = productPrice;
		this.quantity = quantity;
		this.quoteItemPrice = quoteItemPrice;
	}

	/**
	 * Obtains the {@link QuoteItem} identifier.
	 * 
	 * @return {@link QuoteItem} identifier.
	 */
	public Long getQuoteItemId() {
		return this.quoteItemId;
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
	 * Obtains the quoted price for a single {@link Product}.
	 * 
	 * @return Quoted price for a single {@link Product}.
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
	 * Obtains the price for this {@link QuoteItem}.
	 * 
	 * @return Price for this {@link QuoteItem}.
	 */
	public double getQuoteItemPrice() {
		return this.quoteItemPrice;
	}

}