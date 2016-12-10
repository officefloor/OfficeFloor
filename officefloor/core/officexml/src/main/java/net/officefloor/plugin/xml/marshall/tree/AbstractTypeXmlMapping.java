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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Abstract {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} to aid
 * in type specific mapping.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTypeXmlMapping extends AbstractXmlMapping {

	/**
	 * Mapping of concrete type to its context.
	 */
	protected final Map<Class<?>, XmlSpecificContext> contexts = new HashMap<Class<?>, XmlSpecificContext>();

	/**
	 * Contains the configurations for the various types potentially held within
	 * the collection.
	 */
	protected final List<TypeItem> items = new LinkedList<TypeItem>();

	/**
	 * Name of XML element for this mapping.
	 */
	protected final String elementName;

	/**
	 * Starting XML tag to surround the collection.
	 */
	protected final String elementStart;

	/**
	 * Ending XML tag to surround the collection.
	 */
	protected final String elementEnd;

	/**
	 * Initiate with details to map specific sub-types.
	 * 
	 * @param elementName
	 *            Name of XML element for this mapping or <code>null</code> if
	 *            no element.
	 * @param getMethod
	 *            Method to obtain the collection.
	 * @param items
	 *            Configuration for items to match to sub-types.
	 * @param referenceRegistry
	 *            Registry of {@link XmlMapping} that may be referenced.
	 * @param translatorRegistry
	 *            Registry of the
	 *            {@link net.officefloor.plugin.xml.marshall.translate.Translator}
	 *            instances.
	 * @throws XmlMarshallException
	 *             If fails to configure.
	 */
	public AbstractTypeXmlMapping(String elementName, Method getMethod,
			XmlMappingMetaData[] items, TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {
		super(getMethod);

		// Store state
		this.elementName = elementName;

		// Specify surrounding tags
		if (this.elementName == null) {
			this.elementStart = null;
			this.elementEnd = null;
		} else {
			this.elementStart = "<" + this.elementName + ">";
			this.elementEnd = "</" + this.elementName + ">";
		}

		// Iterate over the items
		for (XmlMappingMetaData itemConfig : items) {

			// Ensure item configuration
			if (!XmlMappingType.ITEM.equals(itemConfig.getType())) {
				throw new XmlMarshallException(
						"Direct child configuration of collection must by type "
								+ XmlMappingType.ITEM);
			}

			// Obtain the upper bound on item
			Class<?> upperBound = XmlMarshallerUtil.obtainClass(itemConfig
					.getUpperBoundType());

			// Obtain the element name with surrounding whitespacing removed for
			// the item
			String itemElementName = itemConfig.getElementName();
			if (itemElementName != null) {
				itemElementName = itemElementName.trim();
			}

			// Obtain the configuration for the item
			XmlMappingMetaData[] itemConfiguation = itemConfig
					.getObjectMappings();

			// Create a context for the particular item
			XmlContext context = new XmlContext(upperBound, itemElementName,
					itemConfiguation, false, translatorRegistry,
					referenceRegistry);

			// Add item to configuration listing
			this.items.add(new TypeItem(upperBound, context));

			// Load the configuration (new context each time)
			this.contexts.put(upperBound,
					context.getSpecificContext(upperBound));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.xml.marshall.tree.XmlWriter#writeXml(java.lang
	 * .Object, net.officefloor.plugin.xml.XmlOutput)
	 */
	public void writeXml(Object object, XmlOutput output)
			throws XmlMarshallException {

		// Write opening tag if specified
		if (this.elementName != null) {
			XmlMarshallerUtil.writeXml(this.elementStart, output);
		}

		// Write the specific xml
		this.writeSpecificXml(object, output);

		// Write closing tag if specified
		if (this.elementName != null) {
			XmlMarshallerUtil.writeXml(this.elementEnd, output);
		}
	}

	/**
	 * Writes the specific XML for the input object.
	 * 
	 * @param object
	 *            Object to be written as XML.
	 * @param output
	 *            Output to write the XML.
	 * @throws XmlMarshallException
	 *             If fails to write the XML.
	 */
	protected abstract void writeSpecificXml(Object object, XmlOutput output)
			throws XmlMarshallException;

	/**
	 * Obtains the matching {@link XmlSpecificContext} for the input type.
	 * 
	 * @param specificObject
	 *            Specific object to obtain the {@link XmlSpecificContext}.
	 * @return {@link XmlSpecificContext} specific to input type.
	 * @throws XmlMarshallException
	 *             If fails to obtain a matching {@link XmlSpecificContext}.
	 */
	@SuppressWarnings({ "rawtypes" })
	protected XmlSpecificContext getMatchingContext(Object specificObject)
			throws XmlMarshallException {

		// Obtain type of specific object to determine specific context
		Class type = specificObject.getClass();

		// Check if have cached
		XmlSpecificContext context = this.contexts.get(type);
		if (context != null) {
			// Found as cached
			return context;
		}

		// Must identify matching type
		TypeItem matchingItem = null;
		Iterator<TypeItem> iterator = this.items.iterator();
		while (iterator.hasNext() && (matchingItem == null)) {
			// Obtain current item
			TypeItem currentItem = iterator.next();

			// Check if matching type
			if (currentItem.getUpperBoundType().isAssignableFrom(type)) {
				// Found matching type
				matchingItem = currentItem;
			}
		}

		// Fail if no matching item
		if (matchingItem == null) {
			throw new XmlMarshallException(
					"Unable to find matching item for class " + type.getName());
		}

		// Found matching item therefore obtain context
		context = matchingItem.getContext().getSpecificContext(type);

		// Cache specific type
		this.contexts.put(type, context);

		// Return specific context
		return context;
	}

}

/**
 * Configuration of mapping a particular type item.
 */
class TypeItem {

	/**
	 * Upper bound for matching to item.
	 */
	protected final Class<?> upperBoundType;

	/**
	 * Context for the item.
	 */
	protected final XmlContext context;

	/**
	 * Initiate with details to map a type item.
	 * 
	 * @param upperBoundType
	 *            Upper bound for matching to item.
	 * @param context
	 *            Context of the item.
	 */
	public TypeItem(Class<?> upperBoundType, XmlContext context) {
		// Store state
		this.upperBoundType = upperBoundType;
		this.context = context;
	}

	/**
	 * Obtains the upper bound type of this item.
	 * 
	 * @return Upper bound type of this item.
	 */
	public Class<?> getUpperBoundType() {
		return this.upperBoundType;
	}

	/**
	 * Obtains the {@link XmlContext} for this item.
	 * 
	 * @return {@link XmlContext} for this item.
	 */
	public XmlContext getContext() {
		return this.context;
	}
}