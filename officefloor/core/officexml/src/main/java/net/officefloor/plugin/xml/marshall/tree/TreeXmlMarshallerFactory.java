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

import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * <p>
 * Factory for the creation of a {@link TreeXmlMarshaller}.
 * <p>
 * This is to ease obtaining a {@link TreeXmlMarshaller} but if utilising office
 * floor framework should plug-in via
 * {@link TreeXmlMarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TreeXmlMarshallerFactory {

	/**
	 * Singleton.
	 */
	private static final TreeXmlMarshallerFactory INSTANCE = new TreeXmlMarshallerFactory();

	/**
	 * Enforce singleton.
	 */
	private TreeXmlMarshallerFactory() {
	}

	/**
	 * Obtains the singleton {@link TreeXmlMarshallerFactory}.
	 *
	 * @return Singleton {@link TreeXmlMarshallerFactory}.
	 */
	public static TreeXmlMarshallerFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Creates the {@link TreeXmlMarshaller} from the input configuration.
	 *
	 * @param configuration
	 *            Configuration of the {@link TreeXmlMarshaller}.
	 * @return Configured {@link TreeXmlMarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	public TreeXmlMarshaller createMarshaller(InputStream configuration)
			throws XmlMarshallException {

		// Create the managed object source
		TreeXmlMarshallerManagedObjectSource source = new TreeXmlMarshallerManagedObjectSource(
				configuration);

		// Source the marshaller
		try {
			return (TreeXmlMarshaller) new ManagedObjectUserStandAlone()
					.sourceManagedObject(source).getObject();
		} catch (Throwable ex) {
			throw new XmlMarshallException(ex.getMessage(), ex);
		}
	}

}
