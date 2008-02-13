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
package net.officefloor.plugin.xml.unmarshall.tree;

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
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource} to
 * obtain a
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
 * 
 * @author Daniel
 */
public class TreeXmlUnmarshallerManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSource, ManagedObjectSourceMetaData<D, H> {

	/**
	 * Property name to obtain the {@link java.io.InputStream} for configuring
	 * the {@link TreeXmlUnmarshaller}.
	 */
	protected static final String CONFIGURATION_PROPERTY_NAME = "configuration";

	/**
	 * Meta-data for the {@link TreeXmlUnmarshaller} to enable it to configure
	 * another instance from file.
	 */
	protected static final XmlMappingMetaData XML_OBJECT_MAPPING_META_DATA = new XmlMappingMetaData(
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
	protected XmlMappingMetaData metaData = null;

	/**
	 * Registry of
	 * {@link net.officefloor.plugin.xml.unmarshall.translate.Translator}
	 * instances for mapping.
	 */
	protected TranslatorRegistry translatorRegistry = null;

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
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getMetaData()
	 */
	public ManagedObjectSourceMetaData<D, H> getMetaData() {
		return this;
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

		// Create the translator registry
		this.translatorRegistry = new TranslatorRegistry();

		// Load the meta-data
		this.metaData = createXmlMappingMetaData(configuration,
				this.translatorRegistry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	public void start(ManagedObjectExecuteContext<?> context) throws Exception {
		// No handlers
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		try {
			// Create an instance
			user.setManagedObject(new TreeXmlUnmarshallerManagedObject(
					new TreeXmlUnmarshaller(this.metaData,
							this.translatorRegistry,
							new ReferencedXmlMappingRegistry())));
		} catch (XmlMarshallException ex) {
			// Flag failure
			user.setFailure(ex);
		}
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSourceMetaData#getManagedObjectClass()
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends ManagedObject> getManagedObjectClass() {
		return TreeXmlUnmarshallerManagedObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSourceMetaData#getObjectClass()
	 */
	public Class<?> getObjectClass() {
		return XmlUnmarshaller.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
	 */
	public Class<D> getDependencyKeys() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(D)
	 */
	public ManagedObjectDependencyMetaData getDependencyMetaData(D key) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	public Class<H> getHandlerKeys() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	public Class<? extends Handler<?>> getHandlerType(H key) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
		return null;
	}

}

/**
 * {@link net.officefloor.core.spi.managedobject.ManagedObject} for the
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
 */
class TreeXmlUnmarshallerManagedObject implements ManagedObject {

	/**
	 * {@link TreeXmlUnmarshaller} being managed.
	 */
	protected final TreeXmlUnmarshaller unmarshaller;

	/**
	 * Initiate with the {@link TreeXmlUnmarshaller} to manage.
	 * 
	 * @param unmarshaller
	 *            {@link TreeXmlUnmarshaller} to manage.
	 */
	public TreeXmlUnmarshallerManagedObject(TreeXmlUnmarshaller unmarshaller) {
		// Store state
		this.unmarshaller = unmarshaller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() {
		return this.unmarshaller;
	}

}