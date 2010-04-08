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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to deliver {@link Product} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class DeliverProductsAction extends ActionSupport {

	/**
	 * {@link ProductDelivery} instances.
	 */
	private List<ProductDelivery> products = new LinkedList<ProductDelivery>();

	/**
	 * Obtains the {@link ProductDelivery} instances.
	 * 
	 * @return {@link ProductDelivery} instances.
	 */
	public List<ProductDelivery> getProducts() {

		// Load products from database if not provided by form
		if (this.products.size() == 0) {
			ProductCatalogLocal catalog = ActionUtil
					.lookupService(ProductCatalogLocal.class);
			for (Product product : catalog.retrieveProductList()) {
				this.products.add(new ProductDelivery(product));
			}
		}

		// Return the products
		return this.products;
	}

}