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
package net.officefloor.plugin.gwt.module;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gwt.core.client.EntryPoint;

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
	 * {@link ClassLoader} to obtain the GWT Module template.
	 */
	private final ClassLoader classLoader;

	/**
	 * Prefix for the path to the GWT Module. <code>null</code> indicates no
	 * prefix.
	 */
	private final String pathPrefix;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 * @param classLoader
	 *            {@link ClassLoader} to obtain the GWT Module template.
	 * @param pathPrefix
	 *            Prefix for the path to the GWT Module. <code>null</code>
	 *            indicates no prefix.
	 */
	public GwtModuleRepositoryImpl(ModelRepository modelRepository,
			ClassLoader classLoader, String pathPrefix) {
		this.modelRepository = modelRepository;
		this.classLoader = classLoader;

		// Ensure path prefix ends with '/' separator
		if ((pathPrefix == null) || (pathPrefix.trim().length() == 0)) {
			this.pathPrefix = "";
		} else {
			this.pathPrefix = (pathPrefix.endsWith("/") ? pathPrefix
					: pathPrefix + "/");
		}
	}

	/*
	 * ===================== GwtModuleRepository =========================
	 */

	@Override
	public GwtModuleModel retrieveGwtModule(String gwtModulePath,
			ConfigurationContext context) throws Exception {

		// Obtain the configuration
		ConfigurationItem configuration = context
				.getConfigurationItem(this.pathPrefix + gwtModulePath);
		if (configuration == null) {
			return null; // No configuration, so no GWT Module
		}

		// Return the GWT Module
		return this.modelRepository.retrieve(new GwtModuleModel(),
				configuration);
	}

	@Override
	public String storeGwtModule(GwtModuleModel module,
			ConfigurationContext context, String existingGwtModulePath)
			throws Exception {

		// Obtain the module path
		String modulePath = this.createGwtModulePath(module);

		// Obtain path for storing the GWT Module
		String storeLocation = this.pathPrefix + modulePath;

		// Obtain the existing GWT Module
		String existingLocation = this.pathPrefix + existingGwtModulePath;
		ConfigurationItem existingConfiguration = context
				.getConfigurationItem(existingLocation);

		// Obtain the GWT Module to store (as may already exist at location)
		ConfigurationItem storeConfiguration = context
				.getConfigurationItem(storeLocation);

		// Obtain the content
		InputStream content;
		if (existingConfiguration == null) {
			// No existing GWT Module content, so create
			content = this.createGwtModule(module);
		} else {
			// Update the GWT Module content
			content = this.updateGwtModule(module,
					existingConfiguration.getConfiguration());
		}

		// Store the content
		if (storeConfiguration == null) {
			// No existing GWT Module, so create
			context.createConfigurationItem(storeLocation, content);
		} else {
			// Existing GWT Module, so update its content
			storeConfiguration.setConfiguration(content);
		}

		// Remove the old GWT Module if applicable
		if ((existingConfiguration != null)
				&& (!(storeLocation.equals(existingLocation)))) {
			// Relocated, so remove old GWT Module
			context.deleteConfigurationItem(existingLocation);
		}

		// Return path to the GWT Module
		return modulePath;
	}

	@Override
	public void deleteGwtModule(String gwtModulePath,
			ConfigurationContext context) throws Exception {
		// Delete the configuration
		context.deleteConfigurationItem(this.pathPrefix + gwtModulePath);
	}

	@Override
	public String createGwtModulePath(GwtModuleModel module) {

		// Determine the module path
		String entryPointClassName = module.getEntryPointClassName();
		int index = entryPointClassName.lastIndexOf('.');
		if (index >= 0) {
			index = entryPointClassName
					.lastIndexOf('.', (index - ".".length()));
		}
		String modulePath = entryPointClassName.substring(0, index).replace(
				'.', '/');

		// Obtain the module name
		String moduleName = this.getGwtModuleName(module);

		// Obtain the path to the GWT Module
		modulePath = modulePath + "/" + moduleName + ".gwt.xml";

		// Return the GWT Module path
		return modulePath;
	}

	@Override
	public InputStream createGwtModule(GwtModuleModel module) throws Exception {

		// Obtain the location of template GWT Module
		String templateLocation = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/Template.gwt.xml";

		// Load the Template GWT Module
		InputStream moduleTemplate = this.classLoader
				.getResourceAsStream(templateLocation);
		if (moduleTemplate == null) {
			throw new FileNotFoundException("Can not find GWT Module template "
					+ templateLocation);
		}

		// Return the template with updated details
		return this.updateGwtModule(module, moduleTemplate);
	}

	@Override
	public InputStream updateGwtModule(GwtModuleModel module,
			InputStream existingContent) throws Exception {

		// Only want to change the module configuration and leave rest as is.
		// Therefore loading DOM to be changed and written back.
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setIgnoringElementContentWhitespace(false);
		domFactory.setIgnoringComments(false);
		domFactory.setCoalescing(false);
		Document document = domFactory.newDocumentBuilder().parse(
				existingContent);
		document.setXmlStandalone(true);

		// Obtain the module node
		Element moduleNode = (Element) this.getFirstDirectChild(document,
				"module");
		if (moduleNode == null) {
			throw new IOException(
					"Can not find <module> within configuration.  Please ensure file is a GWT Module");
		}

		// Ensure rename-to attribute is updated
		String moduleName = this.getGwtModuleName(module);
		moduleNode.setAttribute("rename-to", moduleName);

		// Element names
		final String ENTRY_POINT = "entry-point";
		final String SOURCE = "source";
		final String PATH = "path";
		final String INHERITS = "inherits";
		final String NAME = "name";

		// Ensure entry-point class is updated
		String entryPointClassName = module.getEntryPointClassName();
		Element entryPointNode = (Element) this.getFirstDirectChild(moduleNode,
				ENTRY_POINT);
		if (entryPointNode == null) {
			// No entry-point element, so add one before source nodes
			entryPointNode = document.createElement(ENTRY_POINT);
			Element sourceNode = (Element) this.getFirstDirectChild(moduleNode,
					SOURCE);
			moduleNode.insertBefore(entryPointNode, sourceNode);
		}
		entryPointNode.setAttribute("class", entryPointClassName);

		// Ensure source path is included
		String sourcePath = this.getSourcePath(entryPointClassName);
		boolean isSourcePathIncluded = false;
		NodeList children = moduleNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (SOURCE.equals(child.getNodeName())) {
				Element sourceNode = (Element) child;
				if (sourcePath.equals(sourceNode.getAttribute(PATH))) {
					isSourcePathIncluded = true;
				}
			}
		}
		if (!isSourcePathIncluded) {
			// Source path not included so add
			Element sourceNode = document.createElement(SOURCE);
			sourceNode.setAttribute(PATH, sourcePath);
			moduleNode.appendChild(sourceNode);
		}

		// Add any necessary inherits
		for (String inherit : module.getInherits()) {

			// Determine if inherits already included
			boolean isInheritIncluded = false;
			children = moduleNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (INHERITS.equals(child.getNodeName())) {
					Element sourceNode = (Element) child;
					if (inherit.equals(sourceNode.getAttribute(NAME))) {
						isInheritIncluded = true;
					}
				}
			}
			if (!isInheritIncluded) {
				// Inherit not included so add
				Element inheritNode = document.createElement(INHERITS);
				inheritNode.setAttribute(NAME, inherit);
				moduleNode.insertBefore(inheritNode, entryPointNode);
			}
		}

		// Obtain the changed module configuration
		TransformerFactory transformFactory = TransformerFactory.newInstance();
		Transformer transformer = transformFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StringWriter buffer = new StringWriter();
		transformer
				.transform(new DOMSource(document), new StreamResult(buffer));

		// Return the updated configuration
		return this.createConfiguration(buffer.toString());
	}

	/**
	 * Obtains the first direct child {@link Node} by the tag name.
	 * 
	 * @param parent
	 *            Parent to find the first direct child {@link Node}.
	 * @param tagName
	 *            Tag name of the child {@link Node}.
	 * @return First direct child {@link Node} or <code>null</code> if no direct
	 *         child {@link Node} by the tag name.
	 */
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
	 * <p>
	 * Obtains the source path from the {@link EntryPoint} class name.
	 * <p>
	 * The source path is the last package segment name before the
	 * {@link EntryPoint} class name.
	 * 
	 * @param entryPointClassName
	 *            {@link EntryPoint} class name.
	 * @return Source path.
	 */
	private String getSourcePath(String entryPointClassName) {
		int separator = entryPointClassName.lastIndexOf('.');
		String sourcePath = entryPointClassName.substring(0, separator);
		separator = sourcePath.lastIndexOf('.');
		sourcePath = sourcePath.substring(separator + ".".length());
		return sourcePath;
	}

	/**
	 * Creates the configuration for the module content.
	 * 
	 * @param moduleConfiguration
	 *            Module configuration.
	 * @return {@link InputStream} to the module configuration.
	 * @throws Exception
	 *             If fails to create the configuration.
	 */
	private InputStream createConfiguration(String moduleContent)
			throws Exception {
		Charset defaultCharset = Charset.defaultCharset();
		ByteArrayInputStream templateConfiguration = new ByteArrayInputStream(
				moduleContent.getBytes(defaultCharset));
		return templateConfiguration;
	}

	/**
	 * Obtains the {@link GwtModuleModel} name.
	 * 
	 * @param module
	 *            {@link GwtModuleModel}.
	 * @return {@link GwtModuleModel} name.
	 */
	private String getGwtModuleName(GwtModuleModel module) {

		// Obtain the module name
		String moduleName = module.getRenameTo();
		moduleName = GwtHttpTemplateSectionExtension
				.getGwtModuleName(moduleName);

		// Return the module name
		return moduleName;
	}

}