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
