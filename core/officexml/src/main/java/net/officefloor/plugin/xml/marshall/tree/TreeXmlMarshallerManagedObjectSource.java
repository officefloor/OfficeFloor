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

import java.io.InputStream;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;
import net.officefloor.plugin.xml.unmarshall.tree.XmlMappingMetaData;
import net.officefloor.plugin.xml.unmarshall.tree.XmlMappingType;

/**
 * {@link ManagedObjectSource} for the {@link TreeXmlMarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlMarshallerManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Property name to obtain the {@link InputStream} for configuring the
	 * {@link TreeXmlMarshaller}.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME = "configuration";

	/**
	 * Meta-data for the {@link TreeXmlUnmarshaller} to enable it to configure
	 * the meta-data for the {@link TreeXmlMarshaller}.
	 */
	private static final XmlMappingMetaData OBJECT_XML_MAPPING_META_DATA = new XmlMappingMetaData(
			net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
			"marshall",
			new XmlMappingMetaData[] {
					new XmlMappingMetaData(
							XmlMappingType.STATIC,
							"setType",
							net.officefloor.plugin.xml.marshall.tree.XmlMappingType.ROOT
									.toString()),
					new XmlMappingMetaData("marshall@type",
							"setUpperBoundType", null),
					new XmlMappingMetaData("marshall@element",
							"setElementName", null),
					new XmlMappingMetaData("marshall@id", "setId", null),
					new XmlMappingMetaData(
							"attributes",
							"addObjectMapping",
							net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
							new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC,
											"setType",
											net.officefloor.plugin.xml.marshall.tree.XmlMappingType.ATTRIBUTES
													.toString()),
									new XmlMappingMetaData(
											"attribute",
											"addObjectMapping",
											net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
											new XmlMappingMetaData[] {
													new XmlMappingMetaData(
															XmlMappingType.STATIC,
															"setType",
															net.officefloor.plugin.xml.marshall.tree.XmlMappingType.ATTRIBUTE
																	.toString()),
													new XmlMappingMetaData(
															"attribute@attribute",
															"setAttributeName",
															null),
													new XmlMappingMetaData(
															"attribute@method",
															"setGetMethodName",
															null),
													new XmlMappingMetaData(
															"value@isUseRaw",
															"setIsUseRaw", null) },
											"ATTRIBUTE") }, "ATTRIBUTES"),
					new XmlMappingMetaData(
							"value",
							"addObjectMapping",
							net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
							new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC,
											"setType",
											net.officefloor.plugin.xml.marshall.tree.XmlMappingType.VALUE
													.toString()),
									new XmlMappingMetaData("value@element",
											"setElementName", null),
									new XmlMappingMetaData("value@method",
											"setGetMethodName", null),
									new XmlMappingMetaData("value@isUseRaw",
											"setIsUseRaw", null) }, "VALUE"),
					new XmlMappingMetaData(
							"object",
							"addObjectMapping",
							net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
							new XmlMappingMetaData[] {
									new XmlMappingMetaData(
											XmlMappingType.STATIC,
											"setType",
											net.officefloor.plugin.xml.marshall.tree.XmlMappingType.OBJECT
													.toString()),
									new XmlMappingMetaData("object@method",
											"setGetMethodName", null),
									new XmlMappingMetaData("object@element",
											"setElementName", null),
									new XmlMappingMetaData("object@id",
											"setId", null),
									new XmlMappingMetaData("addObjectMapping",
											"ATTRIBUTES"),
									new XmlMappingMetaData("addObjectMapping",
											"VALUE"),
									new XmlMappingMetaData("addObjectMapping",
											"OBJECT"),
									new XmlMappingMetaData(
											"type",
											"addObjectMapping",
											net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
											new XmlMappingMetaData[] {
													new XmlMappingMetaData(
															XmlMappingType.STATIC,
															"setType",
															net.officefloor.plugin.xml.marshall.tree.XmlMappingType.TYPE
																	.toString()),
													new XmlMappingMetaData(
															"type@method",
															"setGetMethodName",
															null),
													new XmlMappingMetaData(
															"type@element",
															"setElementName",
															null),
													new XmlMappingMetaData(
															"type@id", "setId",
															null),
													new XmlMappingMetaData(
															"item",
															"addObjectMapping",
															net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
															new XmlMappingMetaData[] {
																	new XmlMappingMetaData(
																			XmlMappingType.STATIC,
																			"setType",
																			net.officefloor.plugin.xml.marshall.tree.XmlMappingType.ITEM
																					.toString()),
																	new XmlMappingMetaData(
																			"item@type",
																			"setUpperBoundType",
																			null),
																	new XmlMappingMetaData(
																			"item@element",
																			"setElementName",
																			null),
																	new XmlMappingMetaData(
																			"item@id",
																			"setId",
																			null),
																	new XmlMappingMetaData(
																			"addObjectMapping",
																			"ATTRIBUTES"),
																	new XmlMappingMetaData(
																			"addObjectMapping",
																			"VALUE"),
																	new XmlMappingMetaData(
																			"addObjectMapping",
																			"OBJECT"),
																	new XmlMappingMetaData(
																			"addObjectMapping",
																			"TYPE"),
																	new XmlMappingMetaData(
																			"collection",
																			"addObjectMapping",
																			net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
																			new XmlMappingMetaData[] {
																					new XmlMappingMetaData(
																							XmlMappingType.STATIC,
																							"setType",
																							net.officefloor.plugin.xml.marshall.tree.XmlMappingType.COLLECTION
																									.toString()),
																					new XmlMappingMetaData(
																							"collection@method",
																							"setGetMethodName",
																							null),
																					new XmlMappingMetaData(
																							"collection@element",
																							"setElementName",
																							null),
																					new XmlMappingMetaData(
																							"collection@id",
																							"setId",
																							null),
																					new XmlMappingMetaData(
																							"addObjectMapping",
																							"ITEM") },
																			"COLLECTION"),
																	new XmlMappingMetaData(
																			"reference",
																			"addObjectMapping",
																			net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData.class,
																			new XmlMappingMetaData[] {
																					new XmlMappingMetaData(
																							XmlMappingType.STATIC,
																							"setType",
																							net.officefloor.plugin.xml.marshall.tree.XmlMappingType.REFERENCE
																									.toString()),
																					new XmlMappingMetaData(
																							"reference@method",
																							"setGetMethodName",
																							null),
																					new XmlMappingMetaData(
																							"reference@id",
																							"setId",
																							null) },
																			"REFERENCE") },
															"ITEM") }, "TYPE"),
									new XmlMappingMetaData("addObjectMapping",
											"COLLECTION"),
									new XmlMappingMetaData("addObjectMapping",
											"REFERENCE") }, "OBJECT"),
					new XmlMappingMetaData("addObjectMapping", "TYPE"),
					new XmlMappingMetaData("addObjectMapping", "COLLECTION"),
					new XmlMappingMetaData("addObjectMapping", "REFERENCE") });

	/**
	 * Instance of the {@link TreeXmlMarshaller} used for all marshalling from
	 * this source.
	 */
	private TreeXmlMarshallerManagedObject instance;

	/**
	 * Ensure default constructor to enable creation of an instance.
	 */
	public TreeXmlMarshallerManagedObjectSource() {
	}

	/**
	 * Utilised by the {@link TreeXmlMarshallerFactory} to ease creation.
	 * 
	 * @param configuration
	 *            Configuration of the {@link TreeXmlMarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to configure the {@link TreeXmlMarshaller}.
	 */
	TreeXmlMarshallerManagedObjectSource(InputStream configuration)
			throws XmlMarshallException {
		this.loadInstance(configuration);
	}

	/**
	 * Loads the {@link #instance} from the input configuration.
	 * 
	 * @param configuration
	 *            Configuration of the {@link TreeXmlMarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to configure the {@link TreeXmlMarshaller}.
	 */
	private void loadInstance(InputStream configuration)
			throws XmlMarshallException {
		// Create the unmarshaller to configure meta-data of the marshaller
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
				.createUnmarshaller(OBJECT_XML_MAPPING_META_DATA);

		// Load the meta-data
		net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData metaData = new net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData();
		unmarshaller.unmarshall(configuration, metaData);

		// Create the marshaller with the single managed object instance
		this.instance = new TreeXmlMarshallerManagedObject(
				new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
						new ReferencedXmlMappingRegistry()));
	}

	/*
	 * ================== AbstractManagedObjectSource ==========================
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

		// Load the instance
		this.loadInstance(configuration);

		// Load the meta-data details
		context.setManagedObjectClass(TreeXmlMarshallerManagedObject.class);
		context.setObjectClass(TreeXmlMarshaller.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Return the instance
		return this.instance;
	}

	/**
	 * {@link ManagedObject} for the {@link TreeXmlMarshaller}.
	 */
	private static class TreeXmlMarshallerManagedObject implements
			ManagedObject {

		/**
		 * Instance of the {@link TreeXmlMarshaller} for this source.
		 */
		private final TreeXmlMarshaller marshaller;

		/**
		 * Initiate with the {@link TreeXmlMarshaller} to be managed.
		 * 
		 * @param marshaller
		 *            {@link TreeXmlMarshaller} to be managed.
		 */
		public TreeXmlMarshallerManagedObject(TreeXmlMarshaller marshaller) {
			this.marshaller = marshaller;
		}

		/*
		 * ================ ManagedObject ======================================
		 */

		@Override
		public Object getObject() throws Exception {
			return this.marshaller;
		}
	}

}
