/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
