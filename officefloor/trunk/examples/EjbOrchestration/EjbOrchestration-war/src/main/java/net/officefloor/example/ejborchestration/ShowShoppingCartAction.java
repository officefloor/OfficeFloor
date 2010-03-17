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

import javax.naming.Context;
import javax.naming.InitialContext;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to search the {@link ProductCatalog}.
 * 
 * @author Daniel Sagenschneider
 */
public class ShowShoppingCartAction extends ActionSupport {

	@Override
	public String execute() throws Exception {

		// TODO remove
		Context context = new InitialContext();
		Object object = context.lookup(ProductCatalogLocal.class
				.getSimpleName());
		System.out.println("Looked up object: " + object);
		ProductCatalogLocal catalog = (ProductCatalogLocal) object;
		System.out.println("Obtained Product Catalog: " + catalog);

		// Successful
		return SUCCESS;
	}

	/**
	 * Obtains the {@link ShoppingCart}.
	 * 
	 * @return {@link ShoppingCart}.
	 */
	public ShoppingCart getShoppingCart() {
		return new ShoppingCart();
	}

}