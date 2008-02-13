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
package net.officefloor.plugin.xml.unmarshall.flat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.load.ValueLoaderFactory;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}to
 * obtain a
 * {@link net.officefloor.plugin.xml.unmarshall.flat.FlatXmlUnmarshaller}.
 * 
 * @author Daniel
 */
public class FlatXmlUnmarshallerManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSource, ManagedObjectSourceMetaData<D, H> {

	/**
	 * Property name of the {@link Class}of the target object.
	 */
	protected static final String CLASS_PROPERTY_NAME = "class";

	/**
	 * Meta-data for the {@link FlatXmlUnmarshaller}.
	 */
	protected FlatXmlUnmarshallerMetaData metaData = null;

	/**
	 * Pool that instances are cached within.
	 */
	protected ManagedObjectPool resourcePool = null;

	/**
	 * Default contructor to enable creation of an instance.
	 */
	public FlatXmlUnmarshallerManagedObjectSource() {
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
						CLASS_PROPERTY_NAME, "Target class name") };
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {

		// Obtain the properties
		Properties properties = context.getProperties();

		// Obtain the class
		String className = properties.getProperty(CLASS_PROPERTY_NAME);

		// Ensure have the class name
		if ((className == null) || (className.trim().length() == 0)) {
			throw new Exception("Property '" + CLASS_PROPERTY_NAME
					+ "' must be specified.");
		}

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
		for (Iterator<?> iterator = properties.keySet().iterator(); iterator
				.hasNext();) {
			// Obtain the current key
			String currentKey = (String) iterator.next();

			// Do not include class
			if (!CLASS_PROPERTY_NAME.equals(currentKey)) {

				// Obtain the value for the key
				String value = properties.getProperty(currentKey);

				// Add the XML mapping for current key
				xmlMappingsList.add(new XmlMapping(currentKey, value));
			}
		}
		// Obtain as arrary
		XmlMapping[] xmlMappings = (XmlMapping[]) xmlMappingsList
				.toArray(new XmlMapping[0]);

		// Create the registry of translators
		TranslatorRegistry translatorRegistry = new TranslatorRegistry();

		// Create the value loader factor
		ValueLoaderFactory valueLoaderFactory = new ValueLoaderFactory(
				translatorRegistry, targetObjectClass);

		// Create the meta-data
		this.metaData = new FlatXmlUnmarshallerMetaData(valueLoaderFactory,
				xmlMappings);
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
			// Create an instance of the resource
			user.setManagedObject(new FlatXmlUnmarshallerManagedObject(
					new FlatXmlUnmarshaller(this.metaData)));
		} catch (XmlMarshallException ex) {
			// Proprate failure to create instance
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
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getManagedObjectClass()
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends ManagedObject> getManagedObjectClass() {
		return ManagedObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSource#getObjectClass()
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
 * Implementation of the {@link ManagedObject}.
 */
class FlatXmlUnmarshallerManagedObject implements ManagedObject {

	/**
	 * {@link FlatXmlUnmarshaller}being managed.
	 */
	protected final FlatXmlUnmarshaller unmarshaller;

	/**
	 * Initiate with the {@link FlatXmlUnmarshaller}to be managed.
	 * 
	 * @param unmarshaller
	 *            {@link FlatXmlUnmarshaller}to be managed.
	 */
	public FlatXmlUnmarshallerManagedObject(FlatXmlUnmarshaller unmarshaller) {
		// Store state
		this.unmarshaller = unmarshaller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() {
		return this.unmarshaller;
	}

}