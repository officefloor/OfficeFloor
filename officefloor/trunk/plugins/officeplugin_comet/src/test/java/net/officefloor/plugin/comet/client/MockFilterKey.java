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
package net.officefloor.plugin.comet.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Mock Filter Key.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFilterKey implements IsSerializable {

	/**
	 * Filter Text.
	 */
	private String filterText;

	/**
	 * Additional text.
	 */
	private String additionalText;

	/**
	 * Initiate.
	 * 
	 * @param additionalText
	 *            Additional text.
	 */
	public MockFilterKey(String additionalText) {
		this.additionalText = additionalText;
	}

	/**
	 * Default initiate for GWT serialise.
	 */
	public MockFilterKey() {
	}

	/**
	 * Specifies the filter text.
	 * 
	 * @param filterText
	 *            Filter Text.
	 */
	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	/**
	 * Obtains the additional text.
	 * 
	 * @return Additional text.
	 */
	public String getAdditionalText() {
		return this.additionalText;
	}

	/*
	 * ================== Object ========================
	 */

	@Override
	public boolean equals(Object obj) {

		// Always match if match key null
		if (obj == null) {
			return true;
		}

		// Determine if filter
		if (!(obj instanceof MockFilterKey)) {
			return false;
		}
		MockFilterKey that = (MockFilterKey) obj;

		// Match on filter text
		return this.filterText.equals(that.filterText);
	}

}