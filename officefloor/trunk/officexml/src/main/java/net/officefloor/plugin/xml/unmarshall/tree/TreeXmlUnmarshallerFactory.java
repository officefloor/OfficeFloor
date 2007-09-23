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

import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * <p>
 * Factory for the creation of a
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
 * </p>
 * <p>
 * This is to ease obtaining a
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller} but if
 * utilising office floor framework should plugin via
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerManagedObjectSource}.
 * </p>
 * 
 * @author Daniel
 */
public class TreeXmlUnmarshallerFactory {

	/**
	 * Singleton.
	 */
	private static TreeXmlUnmarshallerFactory INSTANCE = new TreeXmlUnmarshallerFactory();

	/**
	 * Enforce singleton.
	 */
	private TreeXmlUnmarshallerFactory() {
	}

	/**
	 * Obtains the singleton {@link TreeXmlUnmarshaller}.
	 * 
	 * @return Singleton {@link TreeXmlUnmarshaller}.
	 */
	public static TreeXmlUnmarshallerFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Creates the {@link TreeXmlUnmarshaller} from the input configuration.
	 * 
	 * @param configuration
	 *            Configuration of the {@link TreeXmlUnmarshaller}.
	 * @return {@link TreeXmlUnmarshaller} from the configuration.
	 * @throws XmlMarshallException
	 *             If fails to create the {@link TreeXmlUnmarshaller}.
	 */
	@SuppressWarnings("unchecked")
	public TreeXmlUnmarshaller createUnmarshaller(InputStream configuration)
			throws XmlMarshallException {
		// Return the created unmarshaller
		return this
				.createUnmarshaller(new TreeXmlUnmarshallerManagedObjectSource(
						configuration));
	}

	/**
	 * Creates the {@link TreeXmlUnmarshaller} from the input configuration.
	 * 
	 * @param configuration
	 *            Configuration of the {@link TreeXmlUnmarshaller}.
	 * @return {@link TreeXmlUnmarshaller} from the configuration.
	 * @throws XmlMarshallException
	 *             If fails to create the {@link TreeXmlUnmarshaller}.
	 */
	@SuppressWarnings("unchecked")
	public TreeXmlUnmarshaller createUnmarshaller(
			XmlMappingMetaData configuration) throws XmlMarshallException {
		// Return the created unmarshaller
		return this
				.createUnmarshaller(new TreeXmlUnmarshallerManagedObjectSource(
						configuration));
	}

	/**
	 * Creates the {@link TreeXmlUnmarshaller} from the input
	 * {@link TreeXmlUnmarshallerManagedObjectSource}.
	 * 
	 * @param source
	 *            {@link TreeXmlUnmarshallerManagedObjectSource}.
	 * @return {@link TreeXmlUnmarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to create teh {@link TreeXmlUnmarshaller}.
	 */
	protected TreeXmlUnmarshaller createUnmarshaller(
			TreeXmlUnmarshallerManagedObjectSource<?, ?> source)
			throws XmlMarshallException {

		// Source the unmarshaller
		try {
			return (TreeXmlUnmarshaller) ManagedObjectUserStandAlone
					.sourceManagedObject(source).getObject();
		} catch (Exception ex) {
			throw new XmlMarshallException(ex.getMessage(), ex);
		}
	}
}
