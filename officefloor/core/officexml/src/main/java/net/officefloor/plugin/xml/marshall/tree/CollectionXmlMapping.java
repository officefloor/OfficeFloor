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

import java.lang.reflect.Method;
import java.util.Collection;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Implementation of {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping}
 * to iterate over a {@link java.util.Collection} object in context.
 * 
 * @author Daniel Sagenschneider
 */
public class CollectionXmlMapping extends AbstractTypeXmlMapping {

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
	public CollectionXmlMapping(String elementName, Method getMethod,
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

		// Know it is a Collection therefore downcast to iterate over
		Collection<?> collection = (Collection<?>) object;

		// Iterate over the collection mapping the contents
		for (Object item : collection) {

			// Obtain specific context to marshall type
			XmlSpecificContext context = this.getMatchingContext(item);

			// Marshall the item
			context.marshall(item, output);
		}
	}

}
