/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.repository;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.plugin.xml.XmlMarshaller;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.marshall.output.FormattedXmlOutput;
import net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshallerFactory;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

/**
 * {@link ModelRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelRepositoryImpl implements ModelRepository {

	/*
	 * ===================== ModelRepository ==================
	 */

	@Override
	public void retrieve(Object model, ConfigurationItem configuration) throws IOException {

		// Obtain input stream to configuration of marshaller
		InputStream unmarshallConfig = model.getClass().getResourceAsStream("UnmarshallConfiguration.xml");
		if (unmarshallConfig == null) {
			throw new IllegalStateException(
					"Unable to configure retrieving the model type " + model.getClass().getName());
		}

		// Specify the unmarshaller
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance().createUnmarshaller(unmarshallConfig);

		// Obtain configuration
		InputStream configInput = configuration.getInputStream();

		try {
			// Configure and register the Model
			unmarshaller.unmarshall(configInput, model);
		} finally {
			// Close the stream
			configInput.close();
		}
	}

	@Override
	public void store(Object model, WritableConfigurationItem configuration) throws IOException {

		// Obtain input stream to configuration of marshaller
		InputStream marshallerConfiguration = model.getClass().getResourceAsStream("MarshallConfiguration.xml");
		if (configuration == null) {
			throw new IllegalStateException("Unable to configure storing the model type " + model.getClass().getName()
					+ ". Can not find MarshallConfiguration.xml");
		}

		// Create the marshaller
		XmlMarshaller marshaller = TreeXmlMarshallerFactory.getInstance().createMarshaller(marshallerConfiguration);

		// Create the writer to output contents
		ByteArrayOutputStream marshalledModel = new ByteArrayOutputStream();
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(marshalledModel));

		// Create the formatted XML Output
		XmlOutput xmlOutput = new FormattedXmlOutput(new XmlOutput() {
			public void write(String xmlSnippet) throws IOException {
				writer.write(xmlSnippet);
			}
		}, "  ");

		// Store the Model
		marshaller.marshall(model, xmlOutput);

		// Ensure byte array contains all marshalled details of model
		writer.flush();

		// Store marshalled model in configuration
		configuration.setConfiguration(new ByteArrayInputStream(marshalledModel.toByteArray()));
	}

}