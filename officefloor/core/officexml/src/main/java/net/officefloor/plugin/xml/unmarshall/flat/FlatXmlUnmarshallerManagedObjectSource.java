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

package net.officefloor.plugin.xml.unmarshall.flat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.xml.unmarshall.load.ValueLoaderFactory;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * {@link ManagedObjectSource} to obtain a {@link FlatXmlUnmarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlatXmlUnmarshallerManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Property name of the {@link Class}of the target object.
	 */
	public static final String CLASS_PROPERTY_NAME = "class";

	/**
	 * Meta-data for the {@link FlatXmlUnmarshaller}.
	 */
	private FlatXmlUnmarshallerMetaData metaData = null;

	/*
	 * ====================== AbstractManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_PROPERTY_NAME, "Target class name");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class name
		String className = mosContext.getProperty(CLASS_PROPERTY_NAME);

		// Obtain the target object class
		Class<?> targetObjectClass;
		try {
			targetObjectClass = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			// Propagate failure
			throw new Exception("Class '" + className
					+ "' of target object not found.", ex);
		}

		// Obtain the listing of XML mappings
		List<XmlMapping> xmlMappingsList = new ArrayList<XmlMapping>();
		for (Iterator<?> iterator = mosContext.getProperties().keySet()
				.iterator(); iterator.hasNext();) {
			// Obtain the current key
			String currentKey = (String) iterator.next();

			// Do not include class
			if (!CLASS_PROPERTY_NAME.equals(currentKey)) {

				// Obtain the value for the key
				String value = mosContext.getProperty(currentKey);

				// Add the XML mapping for current key
				xmlMappingsList.add(new XmlMapping(currentKey, value));
			}
		}
		// Obtain as array
		XmlMapping[] xmlMappings = (XmlMapping[]) xmlMappingsList
				.toArray(new XmlMapping[0]);

		// Create the registry of translators
		TranslatorRegistry translatorRegistry = new TranslatorRegistry();

		// Create the value loader factor
		ValueLoaderFactory valueLoaderFactory = new ValueLoaderFactory(
				translatorRegistry, targetObjectClass);

		// Create the flat XML unmarshaller meta-data
		this.metaData = new FlatXmlUnmarshallerMetaData(valueLoaderFactory,
				xmlMappings);

		// Load the managed object meta-data
		context.setManagedObjectClass(FlatXmlUnmarshallerManagedObject.class);
		context.setObjectClass(FlatXmlUnmarshaller.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new FlatXmlUnmarshallerManagedObject(new FlatXmlUnmarshaller(
				this.metaData));
	}

	/**
	 * Implementation of the {@link ManagedObject}.
	 */
	private static class FlatXmlUnmarshallerManagedObject implements
			ManagedObject {

		/**
		 * {@link FlatXmlUnmarshaller}being managed.
		 */
		private final FlatXmlUnmarshaller unmarshaller;

		/**
		 * Initiate with the {@link FlatXmlUnmarshaller}to be managed.
		 * 
		 * @param unmarshaller
		 *            {@link FlatXmlUnmarshaller}to be managed.
		 */
		public FlatXmlUnmarshallerManagedObject(FlatXmlUnmarshaller unmarshaller) {
			this.unmarshaller = unmarshaller;
		}

		/*
		 * ================== ManagedObject ===================================
		 */

		@Override
		public Object getObject() {
			return this.unmarshaller;
		}
	}

}
