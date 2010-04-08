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

import java.util.List;

import javax.ejb.Local;

/**
 * {@link Local} interface for the {@link ProductCatalog}.
 * 
 * @author Daniel Sagenschneider
 */
@Local
public interface ProductCatalogLocal {

	/**
	 * Creates a {@link Product} in the catalog.
	 * 
	 * @param name
	 *            Name.
	 * @param price
	 *            Price for a single {@link Product}.
	 * @return {@link Product}.
	 */
	Product createProduct(String name, double price);

	/**
	 * Obtains the {@link Product} by its identifier.
	 * 
	 * @param productId
	 *            Identifier of the {@link Product}.
	 * @return {@link Product}.
	 */
	Product retrieveProduct(Long productId);

	/**
	 * Obtains the {@link Product} instances by name.
	 * 
	 * @param productName
	 *            Name of the {@link Product}.
	 * @return {@link Product} instances by name.
	 */
	List<Product> retrieveProductsByName(String productName);

	/**
	 * Retrieves the {@link Product} listing.
	 * 
	 * @return {@link Product} listing.
	 */
	List<Product> retrieveProductList();

}