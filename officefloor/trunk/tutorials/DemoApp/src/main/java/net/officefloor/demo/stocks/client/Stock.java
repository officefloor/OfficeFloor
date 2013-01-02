/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.demo.stocks.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stock.
 * 
 * @author Daniel Sagenschneider
 */
public class Stock implements IsSerializable {

	/**
	 * Identifier of the {@link Stock}.
	 */
	private String marketId;

	/**
	 * Name of the {@link Stock}.
	 */
	private String name;

	/**
	 * Initiate.
	 * 
	 * @param marketId
	 *            Identifier of the {@link Stock}.
	 * @param name
	 *            Name of {@link Stock}.
	 */
	public Stock(String marketId, String name) {
		this.marketId = marketId;
		this.name = name;
	}

	/**
	 * Initiate for GWT serialise.
	 */
	public Stock() {
	}

	/**
	 * Obtains the identifier for the {@link Stock}.
	 * 
	 * @return Identifier for the {@link Stock}.
	 */
	public String getMarketId() {
		return this.marketId;
	}

	/**
	 * Obtains the name of the {@link Stock}.
	 * 
	 * @return Name of the {@link Stock}.
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * ============================ Object =======================
	 */

	@Override
	public boolean equals(Object obj) {

		// Ensure Stock
		if (!(obj instanceof Stock)) {
			return false;
		}
		Stock that = (Stock) obj;

		// Same if same market identifier
		return this.marketId.equals(that.marketId);
	}

}