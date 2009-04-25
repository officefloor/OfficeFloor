/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.repository.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * XML {@link ConfigurationItem}.
 * 
 * @author Daniel
 */
public class XmlConfigurationItem implements ConfigurationItem {

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * XML text.
	 */
	private final String xmlText;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context;

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            Location.
	 * @param xmlText
	 *            XML text.
	 * @param context
	 *            {@link ConfigurationContext}.
	 */
	public XmlConfigurationItem(String location, String xmlText,
			ConfigurationContext context) {
		this.location = location;
		this.xmlText = xmlText;
		this.context = context;
	}

	/*
	 * =================== ConfigurationItem ==================================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public InputStream getConfiguration() throws Exception {
		return new ByteArrayInputStream(this.xmlText.getBytes());
	}

	@Override
	public void setConfiguration(InputStream configuration) throws Exception {
		throw new UnsupportedOperationException(
				"Can not change content of XML context");
	}

	@Override
	public ConfigurationContext getContext() {
		return this.context;
	}

}