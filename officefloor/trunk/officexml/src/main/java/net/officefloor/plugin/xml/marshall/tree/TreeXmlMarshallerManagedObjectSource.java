/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.xml.marshall.tree;

import java.io.InputStream;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.ManagedObjectSourcePropertyImpl;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlMarshaller;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;
import net.officefloor.plugin.xml.unmarshall.tree.XmlMappingMetaData;
import net.officefloor.plugin.xml.unmarshall.tree.XmlMappingType;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for the {@link net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshaller}.
 * 
 * @author Daniel
 */
public class TreeXmlMarshallerManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSource<D, H>, ManagedObjectSourceMetaData<D, H> {

	/**
	 * Property name to obtain the {@link java.io.InputStream} for configuring
	 * the {@link TreeXmlMarshaller}.
	 */
	protected static final String CONFIGURATION_PROPERTY_NAME = "configuration";

	/**
	 * Meta-data for the {@link TreeXmlUnmarshaller} to enable it to configure
	 * the meta-data for the {@link TreeXmlMarshaller}.
	 */
	protected static final XmlMappingMetaData OBJECT_XML_MAPPING_META_DATA = new XmlMappingMetaData(
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
	protected TreeXmlMarshallerManagedObject instance;

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
		// Configure the marshaller
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
	protected void loadInstance(InputStream configuration)
			throws XmlMarshallException {
		// Create the unmarshaller to configure meta-data of the marshaller
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
				.createUnmarshaller(OBJECT_XML_MAPPING_META_DATA);

		// Load the meta-data
		net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData metaData = new net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData();
		unmarshaller.unmarshall(configuration, metaData);

		// Create the marshaller instance with the single managed object
		// instance
		this.instance = new TreeXmlMarshallerManagedObject(
				new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
						new ReferencedXmlMappingRegistry()));
	}

	/*
	 * ====================================================================
	 * ManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getSpecification()
	 */
	public ManagedObjectSourceSpecification getSpecification() {
		return new ManagedObjectSourceSpecification() {
			public ManagedObjectSourceProperty[] getProperties() {
				return new ManagedObjectSourceProperty[] { new ManagedObjectSourcePropertyImpl(
						CONFIGURATION_PROPERTY_NAME, "Configuration file") };
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {
		// Ensure have configuration property name
		String configurationName = context.getProperties().getProperty(
				CONFIGURATION_PROPERTY_NAME);
		if ((configurationName == null) || (configurationName.length() == 0)) {
			throw new Exception("Property '" + CONFIGURATION_PROPERTY_NAME
					+ "' must be specified.");
		}

		// Ensure obtain configuration
		InputStream configuration = context.getResourceLocator()
				.locateInputStream(configurationName);
		if (configuration == null) {
			throw new Exception("Could not find configuration by location '"
					+ configurationName + "'");
		}

		// Load the instance
		this.loadInstance(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getMetaData()
	 */
	public ManagedObjectSourceMetaData<D, H> getMetaData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	public void start(ManagedObjectExecuteContext<H> context) throws Exception {
		// No handlers necessary
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		// Return the instance
		user.setManagedObject(this.instance);
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getManagedObjectClass()
	 */
	public Class<? extends ManagedObject> getManagedObjectClass() {
		return TreeXmlMarshallerManagedObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getObjectClass()
	 */
	public Class<?> getObjectClass() {
		return XmlMarshaller.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
	 */
	public Class<D> getDependencyKeys() {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(D)
	 */
	public ManagedObjectDependencyMetaData getDependencyMetaData(D key) {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	public Class<H> getHandlerKeys() {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	public Class<? extends Handler<?>> getHandlerType(H key) {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
		// No administration
		return null;
	}

}

/**
 * {@link net.officefloor.core.spi.managedobject.ManagedObject} for the
 * {@link net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshaller}.
 */
class TreeXmlMarshallerManagedObject implements ManagedObject {

	/**
	 * Instance of the {@link TreeXmlMarshaller} for this source.
	 */
	protected final TreeXmlMarshaller marshaller;

	/**
	 * Initiate with the {@link TreeXmlMarshaller} to be managed.
	 * 
	 * @param marshaller
	 *            {@link TreeXmlMarshaller} to be managed.
	 */
	public TreeXmlMarshallerManagedObject(TreeXmlMarshaller marshaller) {
		// Store state
		this.marshaller = marshaller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() throws Exception {
		return this.marshaller;
	}

}