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
package net.officefloor.model.generate;

import java.io.File;

import net.officefloor.model.generate.model.ModelMetaData;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ModelRepository;
import net.officefloor.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.repository.filesystem.FileSystemConfigurationItem;

/**
 * Generates all the models.
 * 
 * @author Daniel
 */
public class GraphGenerator {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository repository = new ModelRepository();

	/**
	 * {@link GenericMetaData}.
	 */
	private final GenericMetaData general = new GenericMetaData();

	/**
	 * Generates the models from the configuration in the raw directory and puts
	 * them in the output directory.
	 */
	public void generate(File rawDir, File outputDir) throws Exception {

		// Clear the ouptut directory
		if (outputDir.exists()) {
			// Ensure is a directory
			if (!outputDir.isDirectory()) {
				throw new IllegalArgumentException("Output '"
						+ outputDir.getCanonicalPath()
						+ "' must be a directory");
			}

			// Clear the directory
			for (File child : outputDir.listFiles()) {
				this.deleteFile(child);
			}
		}

		// Generate the objects
		ConfigurationContext context = new FileSystemConfigurationContext(
				outputDir);
		this.generate(rawDir, context, "");
	}

	/**
	 * Generates the models by recursing over files.
	 */
	private void generate(File rawDir, ConfigurationContext context,
			String relativeDir) throws Exception {

		// Recurse generating the objects
		for (File child : rawDir.listFiles()) {
			if (child.isDirectory()) {
				this.generate(child, context, relativeDir + "/"
						+ child.getName());
			} else {
				// File therefore generate the model
				this.generateModel(child, context, relativeDir);
			}
		}
	}

	/**
	 * Generates the Model.
	 */
	private void generateModel(File configurationFile,
			ConfigurationContext context, String relativeDir) throws Exception {

		// Ensure is a model configuration file
		if (!configurationFile.getName().endsWith(".model.xml")) {
			return;
		}

		// Obtain the name
		String name = configurationFile.getName();
		name = name.replaceAll("\\.model\\.xml", "");

		// Obtain the package (also remove leading / )
		String packageName = relativeDir.replace('/', '.');
		packageName = packageName.substring(1);

		// Unmarshall the model
		ModelMetaData model = this.repository.retrieve(new ModelMetaData(),
				new FileSystemConfigurationItem(configurationFile, null));
		model.setName(name);
		model.setPackageName(packageName);

		// Generate the model
		new ModelGenerator(model, this.general).generateModel(context);
	}

	/**
	 * Clears the input directory.
	 * 
	 * @param dir
	 *            Directory to clear.
	 */
	private void deleteFile(File file) throws Exception {

		// Clear the if directory
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				deleteFile(child);
			}
		}

		// Remove the file
		file.delete();
	}

}
