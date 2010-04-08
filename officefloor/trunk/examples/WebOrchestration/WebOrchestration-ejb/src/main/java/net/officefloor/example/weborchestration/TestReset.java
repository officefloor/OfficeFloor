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

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Provides functionality to reset for testing.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class TestReset implements TestResetLocal {

	/**
	 * {@link EntityManager}.
	 */
	@PersistenceContext(unitName = "product-unit")
	private EntityManager entityManager;

	/*
	 * ===================== TestResetLocal ===========================
	 */

	@Override
	public void reset() {
		this.clearEntities(ShoppingCartItem.class,
				ProductAvailability.class, ShoppingCart.class, Product.class,
				Account.class, Customer.class);
	}

	private void clearEntities(Class<?>... entityTypes) {
		for (Class<?> entityType : entityTypes) {
			this.entityManager.createQuery(
					"DELETE FROM " + entityType.getSimpleName())
					.executeUpdate();
		}
	}

}