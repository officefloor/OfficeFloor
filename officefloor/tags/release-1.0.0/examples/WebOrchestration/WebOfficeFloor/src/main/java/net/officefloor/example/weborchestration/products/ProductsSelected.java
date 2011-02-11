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

package net.officefloor.example.weborchestration.products;

import java.util.HashMap;
import java.util.Map;

public class ProductsSelected {

	/**
	 * {@link ProductQuantity} instances.
	 */
	private final Map<String, ProductQuantity> productQuanties = new HashMap<String, ProductQuantity>();

	/**
	 * Loads the {@link ProductQuantity} instances.
	 * 
	 * @param key
	 *            Key identifying the {@link ProductQuantity}.
	 * @param productQuantity
	 *            {@link ProductQuantity} instance.
	 */
	public void setProducts(String key, ProductQuantity productQuantity) {
		productQuantity.setRowIndex(Integer.parseInt(key));
		this.productQuanties.put(key, productQuantity);
	}

	/**
	 * Obtains the {@link ProductQuantity} instances.
	 * 
	 * @return {@link ProductQuantity} instances.
	 */
	public Map<String, ProductQuantity> getProducts() {
		return this.productQuanties;
	}
}