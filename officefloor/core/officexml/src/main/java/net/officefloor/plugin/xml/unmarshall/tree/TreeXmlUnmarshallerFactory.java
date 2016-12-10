/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.unmarshall.tree;

import java.io.InputStream;

import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * <p>
 * Factory for the creation of a {@link TreeXmlUnmarshaller}.
 * </p>
 * <p>
 * This is to ease obtaining a {@link TreeXmlUnmarshaller} but if utilising
 * office floor framework should plug-in via
 * {@link TreeXmlUnmarshallerManagedObjectSource}.
 * </p>
 *
 * @author Daniel Sagenschneider
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
	public TreeXmlUnmarshaller createUnmarshaller(InputStream configuration)
			throws XmlMarshallException {
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
	public TreeXmlUnmarshaller createUnmarshaller(
			XmlMappingMetaData configuration) throws XmlMarshallException {
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
	 *             If fails to create the {@link TreeXmlUnmarshaller}.
	 */
	private TreeXmlUnmarshaller createUnmarshaller(
			TreeXmlUnmarshallerManagedObjectSource source)
			throws XmlMarshallException {
		try {
			return (TreeXmlUnmarshaller) new ManagedObjectUserStandAlone()
					.sourceManagedObject(source).getObject();
		} catch (Throwable ex) {
			throw new XmlMarshallException(ex.getMessage(), ex);
		}
	}

}