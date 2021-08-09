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

package net.officefloor.plugin.xml.unmarshall.tree;

import java.io.InputStream;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.marshall.translate.Translator;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * {@link ManagedObjectSource} to obtain a {@link TreeXmlUnmarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlUnmarshallerManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Property name to obtain the {@link InputStream} for configuring the
	 * {@link TreeXmlUnmarshaller}.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME = "configuration";

	/**
	 * Meta-data for the {@link TreeXmlUnmarshaller} to enable it to configure
	 * another instance from file.
	 */
	private static final XmlMappingMetaData XML_OBJECT_MAPPING_META_DATA = new XmlMappingMetaData(
			XmlMappingMetaData.class,
			"unmarshall",
			new XmlMappingMetaData[] {
					new XmlMappingMetaData("unmarshall@class",
							"setLoadObjectClassName", null),
					new XmlMappingMetaData("unmarshall@node", "setElementName",
							null),
					new XmlMappingMetaData("static",
							"addLoadObjectConfiguration",
							XmlMappingMetaData.class, new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC, "setType",
											XmlMappingType.STATIC.toString()),
									new XmlMappingMetaData("static@method",
											"setLoadMethodName", null),
									new XmlMappingMetaData("static@value",
											"setStaticValue", null),
									new XmlMappingMetaData("static@format",
											"setFormat", null) }, "STATIC"),
					new XmlMappingMetaData("value",
							"addLoadObjectConfiguration",
							XmlMappingMetaData.class, new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC, "setType",
											XmlMappingType.VALUE.toString()),
									new XmlMappingMetaData("value@node",
											"setElementName", null),
									new XmlMappingMetaData("value@method",
											"setLoadMethodName", null),
									new XmlMappingMetaData("value@format",
											"setFormat", null) }, "VALUE"),
					new XmlMappingMetaData(
							"reference",
							"addLoadObjectConfiguration",
							XmlMappingMetaData.class,
							new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC, "setType",
											XmlMappingType.REFERENCE.toString()),
									new XmlMappingMetaData("reference@method",
											"setLoadMethodName", null),
									new XmlMappingMetaData("reference@id",
											"setId", null) }, "REFERENCE"),
					new XmlMappingMetaData("object",
							"addLoadObjectConfiguration",
							XmlMappingMetaData.class, new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC, "setType",
											XmlMappingType.OBJECT.toString()),
									new XmlMappingMetaData("object@node",
											"setElementName", null),
									new XmlMappingMetaData("object@method",
											"setLoadMethodName", null),
									new XmlMappingMetaData("object@class",
											"setLoadObjectClassName", null),
									new XmlMappingMetaData("object@id",
											"setId", null),
									new XmlMappingMetaData(
											"addLoadObjectConfiguration",
											"STATIC"),
									new XmlMappingMetaData(
											"addLoadObjectConfiguration",
											"VALUE"),
									new XmlMappingMetaData(
											"addLoadObjectConfiguration",
											"OBJECT"),
									new XmlMappingMetaData(
											"addLoadObjectConfiguration",
											"REFERENCE") }, "OBJECT") });

	/**
	 * Meta-data for the {@link TreeXmlUnmarshaller}.
	 */
	private XmlMappingMetaData metaData = null;

	/**
	 * Registry of {@link Translator} instances for mapping.
	 */
	private TranslatorRegistry translatorRegistry = null;

	/**
	 * Creates the {@link XmlMappingMetaData} from the input configuration.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @param translatorRegistry
	 *            {@link TranslatorRegistry}.
	 * @return {@link XmlMappingMetaData} from the input configuration.
	 * @throws XmlMarshallException
	 *             If fails to obtain the {@link XmlMappingMetaData}.
	 */
	public static XmlMappingMetaData createXmlMappingMetaData(
			InputStream configuration, TranslatorRegistry translatorRegistry)
			throws XmlMarshallException {

		// Load the meta-data
		XmlMappingMetaData metaData = new XmlMappingMetaData();
		new TreeXmlUnmarshaller(XML_OBJECT_MAPPING_META_DATA,
				translatorRegistry, new ReferencedXmlMappingRegistry())
				.unmarshall(configuration, metaData);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Default constructor to enable creation of an instance.
	 */
	public TreeXmlUnmarshallerManagedObjectSource() {
	}

	/**
	 * Utilised by the {@link TreeXmlUnmarshallerFactory} to simplify creation.
	 * 
	 * @param configuration
	 *            Configuration of the {@link TreeXmlUnmarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to configure this.
	 */
	TreeXmlUnmarshallerManagedObjectSource(InputStream configuration)
			throws XmlMarshallException {

		// Create the translator registry
		this.translatorRegistry = new TranslatorRegistry();

		// Load the meta-data
		this.metaData = createXmlMappingMetaData(configuration,
				this.translatorRegistry);
	}

	/**
	 * Utilised by the {@link TreeXmlUnmarshallerFactory} to simply creation.
	 * 
	 * @param configuration
	 *            Configuration for the {@link TreeXmlUnmarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to configure this.
	 */
	TreeXmlUnmarshallerManagedObjectSource(XmlMappingMetaData configuration)
			throws XmlMarshallException {

		// Create the translator registry
		this.translatorRegistry = new TranslatorRegistry();

		// Load the meta-data
		this.metaData = configuration;
	}

	/*
	 * ================ AbstractManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CONFIGURATION_PROPERTY_NAME, "Configuration file");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the location of the configuration
		String configurationName = mosContext
				.getProperty(CONFIGURATION_PROPERTY_NAME);

		// Ensure obtain configuration
		InputStream configuration = mosContext.getClassLoader()
				.getResourceAsStream(configurationName);
		if (configuration == null) {
			throw new Exception("Could not find configuration by location '"
					+ configurationName + "'");
		}

		// Create the translator registry
		this.translatorRegistry = new TranslatorRegistry();

		// Load the meta-data for the XML unmarshaller
		this.metaData = createXmlMappingMetaData(configuration,
				this.translatorRegistry);

		// Load the managed object meta-data
		context.setManagedObjectClass(TreeXmlUnmarshallerManagedObject.class);
		context.setObjectClass(TreeXmlUnmarshaller.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new TreeXmlUnmarshallerManagedObject(new TreeXmlUnmarshaller(
				this.metaData, this.translatorRegistry,
				new ReferencedXmlMappingRegistry()));
	}

	/**
	 * {@link ManagedObject} for the {@link TreeXmlUnmarshaller}.
	 */
	private static class TreeXmlUnmarshallerManagedObject implements
			ManagedObject {

		/**
		 * {@link TreeXmlUnmarshaller} being managed.
		 */
		private final TreeXmlUnmarshaller unmarshaller;

		/**
		 * Initiate with the {@link TreeXmlUnmarshaller} to manage.
		 * 
		 * @param unmarshaller
		 *            {@link TreeXmlUnmarshaller} to manage.
		 */
		public TreeXmlUnmarshallerManagedObject(TreeXmlUnmarshaller unmarshaller) {
			this.unmarshaller = unmarshaller;
		}

		/*
		 * ================ ManagedObject ===================================
		 */

		@Override
		public Object getObject() {
			return this.unmarshaller;
		}
	}

}
