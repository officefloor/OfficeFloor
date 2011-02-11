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

package net.officefloor.example.weborchestration.deliver;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.example.weborchestration.Product;

/**
 * Delivered {@link Product} instances.
 * 
 * @author daniel
 */
public class DeliveredProducts {

	/**
	 * {@link ProductDelivery} instances by their key.
	 */
	private final Map<String, ProductDelivery> deliveries = new HashMap<String, ProductDelivery>();

	/**
	 * Loads a {@link ProductDelivery}.
	 * 
	 * @param key
	 *            Key of the {@link ProductDelivery}.
	 * @param delivery
	 *            {@link ProductDelivery}.
	 */
	public void setProducts(String key, ProductDelivery delivery) {
		delivery.setRowIndex(key);
		this.deliveries.put(key, delivery);
	}

	/**
	 * Obtains the {@link ProductDelivery} instances.
	 * 
	 * @return {@link ProductDelivery} instances.
	 */
	public Map<String, ProductDelivery> getDeliveries() {
		return this.deliveries;
	}

}