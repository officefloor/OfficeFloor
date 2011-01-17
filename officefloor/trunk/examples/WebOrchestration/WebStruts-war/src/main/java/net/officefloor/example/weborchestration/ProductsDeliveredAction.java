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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.ProductWarehouseLocal;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to handle {@link Product} instances delivered.
 * 
 * @author Daniel Sagenschneider
 */
public class ProductsDeliveredAction extends ActionSupport {

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
		return this.products;
	}

	/*
	 * ========================= ActionSupport ==========================
	 */

	@Override
	public String execute() throws Exception {

		// Obtain the Product Catalog
		ProductCatalogLocal catalog = ActionUtil
				.lookupService(ProductCatalogLocal.class);

		// Process the products
		boolean isError = false;
		Map<ProductDelivery, Integer> quantities = new HashMap<ProductDelivery, Integer>();
		for (ProductDelivery delivery : this.products) {

			// Determine if quantity (handling valid value)
			int quantity;
			try {
				String quantityText = delivery.getQuantity();
				quantity = (ActionUtil.isBlank(quantityText) ? 0 : Integer
						.parseInt(quantityText.trim()));
				if (quantity < 0) {
					isError = true;
					delivery.setError("Must be a positive integer value");
				}
			} catch (NumberFormatException ex) {
				isError = true;
				delivery.setError("Must be an integer value");
				continue; // do not process further
			}

			// Record the quantities
			quantities.put(delivery, new Integer(quantity));
		}
		if (isError) {
			return ERROR;
		}

		// Obtain the warehouse to deliver the products
		ProductWarehouseLocal warehouse = ActionUtil
				.lookupService(ProductWarehouseLocal.class);

		// Deliver the products
		for (ProductDelivery delivery : this.products) {

			// Obtain the Product
			Product product = catalog.retrieveProduct(delivery.getProductId());

			// Obtain the quantity delivered
			int quantity = quantities.get(delivery).intValue();

			// Deliver the product
			warehouse.productDelivered(product, quantity);
		}

		// Successful
		return SUCCESS;
	}

}