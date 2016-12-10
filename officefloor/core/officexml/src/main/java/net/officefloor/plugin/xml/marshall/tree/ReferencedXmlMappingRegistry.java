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
package net.officefloor.plugin.xml.marshall.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping}
 * instances by id.
 * 
 * @author Daniel Sagenschneider
 */
public class ReferencedXmlMappingRegistry {

	/**
	 * Registry of the {@link XmlMapping} instances.
	 */
	protected final Map<String, XmlMapping> registry = new HashMap<String, XmlMapping>();

	/**
	 * Registers the {@link XmlMapping}.
	 * 
	 * @param id
	 *            Id by which to reference the {@link XmlMapping}.
	 * @param xmlMapping
	 *            {@link XmlMapping} to register.
	 */
	public void registerXmlMapping(String id, XmlMapping xmlMapping) {
		this.registry.put(id, xmlMapping);
	}

	/**
	 * Obtains the {@link XmlMapping} registered under the id.
	 * 
	 * @param id
	 *            Id of the {@link XmlMapping}.
	 * @return {@link XmlMapping} registered under id or <code>null</code> if
	 *         none registered by id.
	 */
	public XmlMapping getXmlMapping(String id) {
		return this.registry.get(id);
	}
}
