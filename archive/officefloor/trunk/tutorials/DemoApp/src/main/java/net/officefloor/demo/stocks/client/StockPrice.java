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
 * Price of a {@link Stock}.
 * 
 * @author Daniel Sagenschneider
 */
public class StockPrice implements IsSerializable {

	/**
	 * Bid size.
	 */
	private int bidSize;

	/**
	 * Bid price.
	 */
	private double bidPrice;

	/**
	 * Ask size.
	 */
	private int askSize;

	/**
	 * Ask price.
	 */
	private double askPrice;

	/**
	 * Time stamp of this {@link StockPrice}.
	 */
	private long timestamp;

	/**
	 * Initiate.
	 * 
	 * @param bidSize
	 *            Bid size.
	 * @param bidPrice
	 *            Bid price.
	 * @param askSize
	 *            Ask size.
	 * @param askPrice
	 *            Ask price.
	 * @param timestamp
	 *            Timestamp.
	 */
	public StockPrice(int bidSize, double bidPrice, int askSize,
			double askPrice, long timestamp) {
		this.bidSize = bidSize;
		this.bidPrice = bidPrice;
		this.askSize = askSize;
		this.askPrice = askPrice;
		this.timestamp = timestamp;
	}

	/**
	 * Initiate for GWT serialise.
	 */
	public StockPrice() {
	}

	/**
	 * Obtains the bid size.
	 * 
	 * @return Bid size.
	 */
	public int getBidSize() {
		return this.bidSize;
	}

	/**
	 * Obtains the bid price.
	 * 
	 * @return Bid price.
	 */
	public double getBidPrice() {
		return this.bidPrice;
	}

	/**
	 * Obtains the ask size.
	 * 
	 * @return Ask size.
	 */
	public int getAskSize() {
		return this.askSize;
	}

	/**
	 * Obtains the ask price.
	 * 
	 * @return Ask price.
	 */
	public double getAskPrice() {
		return this.askPrice;
	}

	/**
	 * Obtains the timestamp.
	 * 
	 * @return Timestamp.
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

}