/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorial.stockwatchapp.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Interest in the {@link Stock}.
 * 
 * @author Daniel Sagenschneider
 */
public class StockInterest implements IsSerializable {

	/**
	 * Market identifiers that have interest in.
	 */
	private Set<String> marketIdentifiers = new HashSet<String>();

	/**
	 * Adds a market identifier.
	 * 
	 * @param marketId
	 *            Market identifier.
	 */
	public void addMarketId(String marketId) {
		this.marketIdentifiers.add(marketId);
	}

	/**
	 * Removes the market identifier.
	 * 
	 * @param marketId
	 *            Market identifier.
	 */
	public void removeMarketId(String marketId) {
		this.marketIdentifiers.remove(marketId);
	}

	/*
	 * ====================== Object ======================
	 */

	@Override
	public boolean equals(Object obj) {

		// Should be matched against a Stock for interest filtering
		if (!(obj instanceof Stock)) {
			return false;
		}
		Stock stock = (Stock) obj;

		// Watch all stocks if none specified
		if (this.marketIdentifiers.size() == 0) {
			return true;
		}

		// Equal if stock is of interest
		return this.marketIdentifiers.contains(stock.getMarketId());
	}

}