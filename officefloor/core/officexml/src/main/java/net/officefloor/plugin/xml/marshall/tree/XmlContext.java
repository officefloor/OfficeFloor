/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.marshall.tree;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;

/**
 * Context for XML marhalling of source object.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlContext {

	/**
	 * Name of the element.
	 */
	protected final String elementName;

	/**
	 * Mapping of concrete class to specific context instance to handling
	 * mappings.
	 */
	protected final Map<Class<?>, XmlSpecificContext> contexts = new HashMap<Class<?>, XmlSpecificContext>();

	/**
	 * Registry of
	 * {@link net.officefloor.plugin.xml.marshall.translate.Translator}
	 * instances.
	 */
	protected final TranslatorRegistry translatorRegistry;

	/**
	 * Registry of {@link XmlMapping} that may be referenced.
	 */
	protected final ReferencedXmlMappingRegistry referenceRegistry;

	/**
	 * Configuration for the specific configuration.
	 */
	protected final XmlMappingMetaData[] configuration;

	/**
	 * Child {@link XmlContext} instances of this {@link XmlContext}. Lazy
	 * loaded as may not always have children.
	 */
	protected Map<String, XmlContext> childContexts;

	/**
	 * Initiate the {@link XmlContext} from configuration.
	 * 
	 * @param upperType
	 *            Upper bound on the type of the source object being marshalled.
	 * @param elementName
	 *            Name of XML element for wrapping the context.
	 * @param configuration
	 *            Configuration of this {@link XmlContext}.
	 * @param isCreateSpecificType
	 *            Flags whether the initial specific type is created.
	 * @param translatorRegistry
	 *            Registry of
	 *            {@link net.officefloor.plugin.xml.marshall.translate.Translator}
	 *            instances for translating objects to string values.
	 * @param referenceRegistry
	 *            Registry of {@link XmlMapping} that may be referenced.
	 * @throws XmlMarshallException
	 *             Should this {@link XmlContext} fail to configure.
	 */
	public XmlContext(Class<?> upperType, String elementName,
			XmlMappingMetaData[] configuration, boolean isCreateSpecificType,
			TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry)
			throws XmlMarshallException {
		// Store state
		this.elementName = elementName;
		this.configuration = configuration;
		this.translatorRegistry = translatorRegistry;
		this.referenceRegistry = referenceRegistry;

		// Check if create specific type for upper bound
		if (isCreateSpecificType) {
			this.getSpecificContext(upperType);
		}
	}

	/**
	 * Obtains the {@link XmlSpecificContext} specific to the input type.
	 * 
	 * @param specificType
	 *            Specific type to obtain the {@link XmlSpecificContext} for.
	 * @return {@link XmlSpecificContext} for the input type.
	 * @throws XmlMarshallException
	 *             If fails to obtain the {@link XmlSpecificContext}.
	 */
	public XmlSpecificContext getSpecificContext(Class<?> specificType)
			throws XmlMarshallException {

		// Check if already cached
		XmlSpecificContext specificContext = this.contexts.get(specificType);
		if (specificContext == null) {
			// Not cached, therefore create and cache
			specificContext = new XmlSpecificContext(specificType,
					this.elementName, this.configuration, this,
					this.translatorRegistry, this.referenceRegistry);
			this.contexts.put(specificType, specificContext);
		}

		// Return specific context for input type
		return specificContext;
	}

	/**
	 * Marshalls the source object as XML to the output.
	 * 
	 * @param source
	 *            Source object to marshall.
	 * @param output
	 *            Output to send marshalled XML.
	 * @throws XmlMarshallException
	 *             If fails to marshall source object.
	 */
	public void marshall(Object source, XmlOutput output)
			throws XmlMarshallException {

		// Obtain the context instance
		XmlSpecificContext specificContext;

		// Ensure have object to marshall
		if (source != null) {

			// Utilise context specific to concrete type of source object
			Class<?> sourceType = source.getClass();

			// Attempt to obtain existing specific context
			specificContext = this.getSpecificContext(sourceType);

			// Have specific context thus marshall
			specificContext.marshall(source, output);
		}
	}

	/**
	 * Obtains the child {@link XmlContext} registered under the reference.
	 * 
	 * @param reference
	 *            Reference to identify the child {@link XmlContext}.
	 * @return Child {@link XmlContext} or <code>null</code> if no child
	 *         {@link XmlContext}by reference.
	 */
	public XmlContext getChildContext(String reference) {

		// Check if child contexts
		if (this.childContexts == null) {
			// No child contexts
			return null;
		}

		// Have child contexts thus return appropriate
		return this.childContexts.get(reference);
	}

	/**
	 * Registers a child {@link XmlContext} of this {@link XmlContext}.
	 * 
	 * @param reference
	 *            Reference to identify the child {@link XmlContext}.
	 * @param childContext
	 *            Child {@link XmlContext} to register.
	 */
	public void registerChildContext(String reference, XmlContext childContext) {
		// Lazy load child contexts
		if (this.childContexts == null) {
			this.childContexts = new HashMap<String, XmlContext>();
		}

		// Register the child context
		this.childContexts.put(reference, childContext);
	}

}
