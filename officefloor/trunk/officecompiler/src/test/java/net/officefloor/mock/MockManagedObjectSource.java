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
package net.officefloor.mock;

import java.util.Properties;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * Mock
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class MockManagedObjectSource implements ManagedObjectSource,
		ManagedObjectSourceMetaData, ManagedObject {

	/**
	 * Properties.
	 */
	private Properties properties;

	/**
	 * Default constructor.
	 */
	public MockManagedObjectSource() {
	}

	/**
	 * Obtains the configured properties.
	 * 
	 * @return Properties.
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * ====================================================================
	 * ManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getSpecification()
	 */
	public ManagedObjectSourceSpecification getSpecification() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {
		// Store details
		this.properties = context.getProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getMetaData()
	 */
	public ManagedObjectSourceMetaData getMetaData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	public void start(ManagedObjectExecuteContext context) throws Exception {
		// Nothing currently for starting
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		// Load this as managed object
		user.setManagedObject(this);
	}

	/*
	 * ====================================================================
	 * ManagedObject
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceMetaData
	 * ====================================================================
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getManagedObjectClass()
	 */
	public Class getManagedObjectClass() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getObjectClass()
	 */
	public Class getObjectClass() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
	 */
	public Class getDependencyKeys() {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(D)
	 */
	public ManagedObjectDependencyMetaData getDependencyMetaData(Enum key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	public Class getHandlerKeys() {
		// No handlers
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	public Class getHandlerType(Enum key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData[] getExtensionInterfacesMetaData() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}
