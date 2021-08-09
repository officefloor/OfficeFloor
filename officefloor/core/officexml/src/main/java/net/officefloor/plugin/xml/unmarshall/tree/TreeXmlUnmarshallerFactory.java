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
