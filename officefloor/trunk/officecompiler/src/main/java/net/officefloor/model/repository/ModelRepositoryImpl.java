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
package net.officefloor.model.repository;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshaller;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.marshall.output.FormattedXmlOutput;
import net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshallerFactory;
import net.officefloor.plugin.xml.unmarshall.designate.DesignateXmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

/**
 * Repository to Models.
 * 
 * @author Daniel
 */
public class ModelRepositoryImpl {

	/**
	 * Registry of {@link XmlUnmarshaller} instances to the class type they
	 * unmarshall.
	 */
	private final Map<Class<?>, XmlUnmarshaller> unmarshallers = new HashMap<Class<?>, XmlUnmarshaller>();

	/**
	 * Registry of {@link XmlMarshaller} instances to the class type they
	 * marshall.
	 */
	private final Map<Class<?>, XmlMarshaller> marshallers = new HashMap<Class<?>, XmlMarshaller>();

	/**
	 * {@link DesignateXmlUnmarshaller}.
	 */
	private final DesignateXmlUnmarshaller designateUnmarshaller = new DesignateXmlUnmarshaller();

	/**
	 * Stores the model within the input configuration.
	 * 
	 * @param model
	 *            Model to be stored.
	 * @param configuration
	 *            Configuration to contain the Model.
	 * @throws Exception
	 *             If fails to store the Model.
	 */
	public void store(Object model, ConfigurationItem configuration)
			throws Exception {

		// Obtain the XmlMarshaller for the Model
		XmlMarshaller marshaller = this.obtainMarshaller(model.getClass());

		// Create the writer to output contents
		ByteArrayOutputStream marshalledModel = new ByteArrayOutputStream();
		final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(marshalledModel));

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
		configuration.setConfiguration(new ByteArrayInputStream(marshalledModel
				.toByteArray()));
	}

	/**
	 * Creates the {@link ConfigurationItem} to hold the Model for the input Id.
	 * 
	 * @param id
	 *            Id to create the {@link ConfigurationItem}.
	 * @param model
	 *            Model to be stored in the created {@link ConfigurationItem}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @return {@link ConfigurationItem} created for the Model.
	 * @throws Exception
	 *             If fails to create the {@link ConfigurationItem}.
	 */
	public ConfigurationItem create(String id, Object model,
			ConfigurationContext context) throws Exception {

		// Obtain the XmlMarshaller for the Model
		XmlMarshaller marshaller = this.obtainMarshaller(model.getClass());

		// Create the writer to output contents
		ByteArrayOutputStream marshalledModel = new ByteArrayOutputStream();
		final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(marshalledModel));

		// Create the XML Output
		XmlOutput xmlOutput = new XmlOutput() {
			public void write(String xmlSnippet) throws IOException {
				writer.write(xmlSnippet);
			}
		};

		// Store the Model
		marshaller.marshall(model, xmlOutput);

		// Ensure byte array contains all marshalled details of model
		writer.flush();

		// Create the configuration with the marshalled model
		return context.createConfigurationItem(id, new ByteArrayInputStream(
				marshalledModel.toByteArray()));
	}

	/**
	 * Configures the input Model from the input configuration.
	 * 
	 * @param model
	 *            Model to be configured.
	 * @param configuration
	 *            Configuration of the Model.
	 * @return Configured Model.
	 * @throws Exception
	 *             If fails to configure the Model.
	 */
	public <O> O retrieve(O model, ConfigurationItem configuration)
			throws Exception {

		// Obtain the XmlUnmarshaller for the Model
		XmlUnmarshaller unmarshaller = this
				.obtainUnmarshaller(model.getClass());

		// Obtain configuration
		InputStream configInput = configuration.getConfiguration();

		try {
			// Configure and register the Model
			unmarshaller.unmarshall(configInput, model);
		} finally {
			// Close the stream
			configInput.close();
		}

		// Return the configured model
		return model;
	}

	/**
	 * Registers meta-data for a model to be retrieved.
	 * 
	 * @param modelType
	 *            {@link Class} of the model.
	 * @throws Exception
	 *             If fails to configure the {@link DesignateXmlUnmarshaller}.
	 */
	public void registerModel(Class<?> modelType) throws Exception {
		// Register the model
		synchronized (this) {
			// Obtain the input stream to configure the unmarshaller
			InputStream unmashallConfig = modelType
					.getResourceAsStream("UnmarshallConfiguration.xml");
			if (unmashallConfig == null) {
				throw new IllegalStateException(
						"Unable to configure retrieving the model type "
								+ modelType.getName());
			}

			// Configure the unmarshaller
			this.designateUnmarshaller
					.registerTreeXmlUnmarshaller(unmashallConfig);
		}
	}

	/**
	 * <p>
	 * Retrieves the model from the input configuration.
	 * <p>
	 * Only models successfully registered by {@link #registerModel(Class)} may
	 * be retrieved by this method.
	 * 
	 * @param configuration
	 *            Configuration of the model.
	 * @return Model of the configuration.
	 * @throws Exception
	 *             If fails to retrieve the model.
	 */
	public Object retrieve(ConfigurationItem configuration) throws Exception {
		return this.designateUnmarshaller.unmarshall(configuration
				.getConfiguration());
	}

	/**
	 * Obtains the {@link XmlUnmarshaller} for the Class of the Model.
	 * 
	 * @param modelClass
	 *            Class of the model.
	 * @return {@link XmlUnmarshaller} for the Model.
	 * @throws Exception
	 *             If fails to obtain the {@link XmlUnmarshaller}.
	 */
	private XmlUnmarshaller obtainUnmarshaller(Class<?> modelClass)
			throws Exception {
		// Obtain the xml unmarshaller to load model (lazy load)
		XmlUnmarshaller unmarshaller = null;
		synchronized (this) {
			// Attempt to obtain from registry
			unmarshaller = this.unmarshallers.get(modelClass);
			if (unmarshaller == null) {
				// Obtain input stream to configuration of marshaller
				InputStream unmarshallConfig = modelClass
						.getResourceAsStream("UnmarshallConfiguration.xml");
				if (unmarshallConfig == null) {
					throw new IllegalStateException(
							"Unable to configure retrieving the model type "
									+ modelClass.getName());
				}

				// Specify the unmarshaller
				unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
						.createUnmarshaller(unmarshallConfig);

				// Register the unmarshaller
				this.unmarshallers.put(modelClass, unmarshaller);
			}
		}

		// Return the unmarshaller
		return unmarshaller;
	}

	/**
	 * Obtains the {@link XmlMarshaller} for the Class of the Model.
	 * 
	 * @param modelClass
	 *            Class of the Model.
	 * @return {@link XmlMarshaller} for the Model.
	 * @throws Exception
	 *             If fails to obtain the {@link XmlMarshaller}.
	 */
	private XmlMarshaller obtainMarshaller(Class<?> modelClass) throws Exception {
		// Obtain the xml marshaller to save office (lazy load)
		XmlMarshaller marshaller = null;
		synchronized (this) {
			// Attempt to obtain from registry
			marshaller = this.marshallers.get(modelClass);
			if (marshaller == null) {
				// Obtain input stream to configuration of marshaller
				InputStream configuration = modelClass
						.getResourceAsStream("MarshallConfiguration.xml");
				if (configuration == null) {
					throw new IllegalStateException(
							"Unable to configure storing the model type "
									+ modelClass.getName()
									+ ". Can not find MarshallConfiguration.xml");
				}

				// Specify the marshaller
				marshaller = TreeXmlMarshallerFactory.getInstance()
						.createMarshaller(configuration);

				// Register the marshaller
				this.marshallers.put(modelClass, marshaller);
			}
		}

		// Return the marshaller
		return marshaller;
	}

}