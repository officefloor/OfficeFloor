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

import java.util.List;

import javax.ejb.Local;

/**
 * {@link Local} interface for the {@link ProductWarehouse}.
 * 
 * @author Daniel Sagenschneider
 */
@Local
public interface ProductWarehouseLocal {

	/**
	 * Obtains the {@link ProductAvailability} for the {@link Product}.
	 * 
	 * @param product
	 *            {@link Product}.
	 * @return {@link ProductAvailability}.
	 */
	ProductAvailability retrieveProductAvailability(Product product);

	/**
	 * Allocates the {@link Product} instances from this
	 * {@link ProductWarehouse}.
	 * 
	 * @param invoice
	 *            {@link Invoice}.
	 * @return {@link ProductOrder} instances. Returns <code>null</code> if
	 *         {@link Invoice} was fulfilled.
	 */
	List<ProductOrder> allocateProduct(Invoice invoice);

	/**
	 * {@link Product} has been delivered to the {@link ProductWarehouse} and
	 * therefore more of the {@link Product} is available.
	 * 
	 * @param product
	 *            {@link Product}.
	 * @param quantityDelivered
	 *            Quantity of {@link Product} delivered to the
	 *            {@link ProductWarehouse}.
	 * @return Potential {@link Invoice} instances that are now fulfilled by
	 *         delivery of the {@link Product} instances.
	 */
	List<Invoice> productDelivered(Product product, int quantityDelivered);

}