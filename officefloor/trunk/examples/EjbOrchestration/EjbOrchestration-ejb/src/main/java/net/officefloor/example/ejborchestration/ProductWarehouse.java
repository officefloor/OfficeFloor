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

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * {@link Product} warehouse {@link Stateless} bean.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class ProductWarehouse implements ProductWarehouseLocal {

	/**
	 * {@link EntityManager}.
	 */
	@PersistenceContext(unitName = "product-unit")
	private EntityManager entityManager;

	/*
	 * ==================== ProductWarehouseLocal ==========================
	 */

	@Override
	public ProductAvailability retrieveProductAvailability(Product product) {

		// Obtain the availability
		ProductAvailability availability = this.entityManager.find(
				ProductAvailability.class, product.getProductId());
		if (availability == null) {
			// Default the availability to zero
			availability = new ProductAvailability(product, 0);
			this.entityManager.persist(availability);
		}

		// Return the availability
		return availability;
	}

	@Override
	public void incrementProductAvailability(Product product,
			int numberAvailable) {

		// Retrieve the Product Availability
		ProductAvailability availability = this
				.retrieveProductAvailability(product);

		// Increment the availability
		availability.setNumberAvailable(availability.getNumberAvailable()
				+ numberAvailable);
	}

}