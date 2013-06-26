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
package net.officefloor.model.impl.repository.inputstream;

import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ReadOnlyConfigurationException;

/**
 * {@link ConfigurationItem} to wrap an {@link InputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputStreamConfigurationItem implements ConfigurationItem {

	/**
	 * {@link InputStream}.
	 */
	private final InputStream inputStream;

	/**
	 * Initiate.
	 * 
	 * @param inputStream
	 *            {@link InputStream}.
	 */
	public InputStreamConfigurationItem(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/*
	 * ======================== ConfigurationItem =========================
	 */

	@Override
	public String getLocation() {
		return InputStream.class.getSimpleName();
	}

	@Override
	public InputStream getConfiguration() throws Exception {
		return this.inputStream;
	}

	@Override
	public void setConfiguration(InputStream configuration) throws Exception,
			ReadOnlyConfigurationException {
		throw new UnsupportedOperationException(
				InputStreamConfigurationItem.class.getSimpleName()
						+ " can not change configuration");
	}

	@Override
	public ConfigurationContext getContext() {
		throw new UnsupportedOperationException("No context available for "
				+ InputStreamConfigurationItem.class.getSimpleName());
	}

}