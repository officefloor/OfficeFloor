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

package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Implementation of {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping}
 * to map an object based on its specific sub-type implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeXmlMapping extends AbstractTypeXmlMapping {

	/**
	 * Initiate with details to marshall the {@link java.util.Collection}.
	 * 
	 * @param elementName
	 *            Name of XML element for this mapping or <code>null</code> if
	 *            no element.
	 * @param getMethod
	 *            Method to obtain the collection.
	 * @param items
	 *            Configuration for items within the
	 *            {@link java.util.Collection}.
	 * @param referenceRegistry
	 *            Registry of {@link XmlMapping} that may be referenced.
	 * @param translatorRegistry
	 *            Registry of the
	 *            {@link net.officefloor.plugin.xml.marshall.translate.Translator}
	 *            instances.
	 * @throws XmlMarshallException
	 *             If fails to configure.
	 */
	public TypeXmlMapping(String elementName, Method getMethod,
			XmlMappingMetaData[] items, TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {
		super(elementName, getMethod, items, translatorRegistry,
				referenceRegistry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTypeXmlMapping#writeSpecificXml(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	protected void writeSpecificXml(Object object, XmlOutput output)
			throws XmlMarshallException {

		// Obtain specific context for object
		XmlSpecificContext context = this.getMatchingContext(object);

		// Marshall the object
		context.marshall(object, output);
	}

}
