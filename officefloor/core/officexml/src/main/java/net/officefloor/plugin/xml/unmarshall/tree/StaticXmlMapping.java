/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.load.StaticValueLoader;

/**
 * {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} that loads a
 * static value to the target object.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticXmlMapping implements XmlMapping {

	/**
	 * Loader to load the staic value.
	 */
	protected final StaticValueLoader loader;

	/**
	 * Initiate with static value loader.
	 * 
	 * @param loader
	 *            Loads the static value onto the target object.
	 */
	public StaticXmlMapping(StaticValueLoader loader) {
		// Store state
		this.loader = loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Load static value to current target object
		this.loader.loadValue(state.getCurrentTargetObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Do nothing
	}

}
