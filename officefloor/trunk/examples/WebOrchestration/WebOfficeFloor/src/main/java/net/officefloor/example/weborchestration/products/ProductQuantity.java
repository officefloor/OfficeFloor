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
package net.officefloor.example.weborchestration.products;

import net.officefloor.example.weborchestration.Product;

/**
 * Quantity of {@link Product}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProductQuantity {

	/**
	 * Index of the row for the {@link Product}.
	 */
	private int rowIndex = -1;

	/**
	 * {@link Product} identifier.
	 */
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
	 * Quantity.
	 */
	private String quantity;

	/**
	 * Error.
	 */
	private String error;

	/**
	 * Default constructor for struts.
	 */
	public ProductQuantity() {
	}

	/**
	 * Initiate.
	 * 
	 * @param rowIndex
	 *            Index of the row for the {@link Product}.
	 * @param product
	 *            {@link Product}.
	 */
	public ProductQuantity(int rowIndex, Product product) {
		this.rowIndex = rowIndex;
		this.productId = product.getProductId();
		this.name = product.getName();
		this.price = product.getPrice();
		this.quantity = "";
		this.error = "";
	}

	/**
	 * Obtains the index of the row for the {@link Product}.
	 * 
	 * @return Row index.
	 */
	public int getRowIndex() {
		return this.rowIndex;
	}

	/**
	 * Specifies the row index.
	 * 
	 * @param rowIndex
	 *            Row index.
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
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
	 * Specifies the {@link Product} identifier.
	 * 
	 * @param productId
	 *            {@link Product} identifier.
	 */
	public void setProductId(Long productId) {
		this.productId = productId;
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

	/**
	 * Obtains the price of a single {@link Product}.
	 * 
	 * @return Price of a single {@link Product}.
	 */
	public double getPrice() {
		return this.price;
	}

	/**
	 * Specifies the price for a single {@link Product}.
	 * 
	 * @param price
	 *            Price for a single {@link Product}.
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Obtains the quantity of {@link Product}.
	 * 
	 * @return Quantity of {@link Product}.
	 */
	public String getQuantity() {
		return this.quantity;
	}

	/**
	 * Specifies the quantity of {@link Product}.
	 * 
	 * @param quantity
	 *            Quantity of {@link Product}.
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * Obtains the error.
	 * 
	 * @return Error.
	 */
	public String getError() {
		return this.error;
	}

	/**
	 * Specifies the error.
	 * 
	 * @param error
	 *            Error.
	 */
	public void setError(String error) {
		this.error = error;
	}

}