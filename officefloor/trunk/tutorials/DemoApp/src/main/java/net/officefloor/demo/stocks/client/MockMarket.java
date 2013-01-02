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

/**
 * Mock market.
 * 
 * @author Daniel Sagenschneider
 */
public class MockMarket {

	/**
	 * Listed {@link Stock} instances for the mock market.
	 */
	public static final MockStock[] listedStocks = new MockStock[] {
			new MockStock("B", "Brokers Ltd", 3.02),
			new MockStock("F", "Futures Fund", 2.93),
			new MockStock("L", "LFTF Resources", 3.07),
			new MockStock("M", "MCK Ltd", 3.04),
			new MockStock("X", "XMPL PLC", 3.08),
			new MockStock("Z", "ZBL Group", 2.96) };

	/**
	 * Mock {@link Stock}.
	 */
	public static class MockStock {

		/**
		 * Market identifier.
		 */
		public final String marketId;

		/**
		 * {@link Stock} name.
		 */
		public final String name;

		/**
		 * Base price for simulation.
		 */
		public final double basePrice;

		/**
		 * Current price for simulation.
		 */
		public double currentPrice;

		/**
		 * Last time prices reset to open.
		 */
		public long lastResetTime = -1;

		/**
		 * Initiate.
		 * 
		 * @param marketId
		 *            Market identifier.
		 * @param name
		 *            {@link Stock} name.
		 * @param basePrice
		 *            Base price for simulation.
		 */
		public MockStock(String marketId, String name, double basePrice) {
			this.marketId = marketId;
			this.name = name;
			this.basePrice = basePrice;
			this.currentPrice = basePrice;
		}
	}

}