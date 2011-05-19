/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.gwt.module;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link GwtModuleRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtModuleRepositoryImpl implements GwtModuleRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public GwtModuleRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== GwtModuleRepository =========================
	 */

	@Override
	public GwtModuleModel retrieveGwtModule(ConfigurationItem configuration)
			throws Exception {
		return this.modelRepository.retrieve(new GwtModuleModel(),
				configuration);
	}

	@Override
	public void createGwtModule(GwtModuleModel module,
			ConfigurationItem configuration) throws Exception {

		// Obtain the location of template GWT Module
		final String TEMPLATE_LOCATION = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/Template.gwt.xml";

		// Load the Template GWT Module
		ConfigurationItem templateItem = configuration.getContext()
				.getConfigurationItem(TEMPLATE_LOCATION);
		if (templateItem == null) {
			throw new FileNotFoundException("Can not find GWT Module template "
					+ TEMPLATE_LOCATION);
		}
		StringWriter buffer = new StringWriter();
		Reader templateReader = new InputStreamReader(
				templateItem.getConfiguration());
		for (int character = templateReader.read(); character != -1; character = templateReader
				.read()) {
			buffer.write(character);
		}
		String template = buffer.toString();

		// Fill out the template
		template = template.replace("${rename.to}", module.getRenameTo());
		template = template.replace("${entry.point.class.name}",
				module.getEntryPointClassName());

		// Write configuration for creating module
		this.writeConfiguration(template, configuration);
	}

	@Override
	public void updateGwtModule(GwtModuleModel module,
			ConfigurationItem configuration) throws Exception {

		// Only want to change the module configuration and leave rest as is.
		// Therefore loading DOM to be changed and written back.
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setIgnoringElementContentWhitespace(false);
		domFactory.setIgnoringComments(false);
		domFactory.setCoalescing(false);
		Document document = domFactory.newDocumentBuilder().parse(
				configuration.getConfiguration());
		document.setXmlStandalone(true);

		// Obtain the module node
		Element moduleNode = (Element) this.getFirstDirectChild(document,
				"module");
		if (moduleNode == null) {
			throw new IOException(
					"Can not find <module> within configuration.  Please ensure file is a GWT Module");
		}

		// Ensure rename-to attribute is updated
		moduleNode.setAttribute("rename-to", module.getRenameTo());

		// Ensure entry-point class is updated
		final String ENTRY_POINT = "entry-point";
		Element entryPointNode = (Element) this.getFirstDirectChild(moduleNode,
				ENTRY_POINT);
		if (entryPointNode == null) {
			// No entry-point element, so add one before source nodes
			entryPointNode = document.createElement(ENTRY_POINT);
			Element sourceNode = (Element) this.getFirstDirectChild(moduleNode,
					"source");
			moduleNode.insertBefore(entryPointNode, sourceNode);
		}
		entryPointNode.setAttribute("class", module.getEntryPointClassName());

		// Obtain the changed module configuration
		TransformerFactory transformFactory = TransformerFactory.newInstance();
		Transformer transformer = transformFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StringWriter buffer = new StringWriter();
		transformer
				.transform(new DOMSource(document), new StreamResult(buffer));

		// Write the updated configuration
		this.writeConfiguration(buffer.toString(), configuration);
	}

	private Node getFirstDirectChild(Node parent, String tagName) {

		// Search for the first direct child with Tag Name
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (tagName.equals(child.getNodeName())) {
				return child; // found direct child by Tag Name
			}
		}

		// As here, did not find child
		return null;
	}

	/**
	 * Writes the module configuration to the {@link ConfigurationItem}.
	 * 
	 * @param moduleConfiguration
	 *            Module configuration.
	 * @param configurationItem
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to write the configuration.
	 */
	private void writeConfiguration(String moduleConfiguration,
			ConfigurationItem configurationItem) throws Exception {
		Charset defaultCharset = Charset.defaultCharset();
		ByteArrayInputStream templateConfiguration = new ByteArrayInputStream(
				moduleConfiguration.getBytes(defaultCharset));
		configurationItem.setConfiguration(templateConfiguration);
	}

}