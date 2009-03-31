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
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Generates all the models.
 * 
 * @author Daniel
 */
public class GraphGenerator {

	/**
	 * Generates the models from the configuration in the raw directory and puts
	 * them in the output directory.
	 */
	public void generate(File rawDir, File outputDir) throws Exception {

		// Create the top level graph meta-data
		GraphNodeMetaData graphMetaData = new GraphNodeMetaData("", rawDir);

		// Clear the output directory
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
		this.generate(rawDir, context, graphMetaData);
	}

	/**
	 * Generates the models by recursing over files.
	 */
	private void generate(File rawDir, ConfigurationContext context,
			GraphNodeMetaData graphMetaData) throws Exception {

		// Recurse generating the objects
		for (File child : rawDir.listFiles()) {
			if (child.isDirectory()) {

				// Create the generic model for child directory
				GraphNodeMetaData childGraphMetaData = new GraphNodeMetaData(
						graphMetaData, child.getName());

				// Recursively generate for sub directories
				this.generate(child, context, childGraphMetaData);
			} else {
				// File therefore generate the model
				this.generateModel(child, context, graphMetaData);
			}
		}
	}

	/**
	 * Generates the Model.
	 */
	private void generateModel(File configurationFile,
			ConfigurationContext context, GraphNodeMetaData graphMetaData)
			throws Exception {

		// Obtain the model meta-data
		ModelMetaData model = graphMetaData.getModelMetaData(configurationFile);
		if (model == null) {
			return; // not a model
		}

		// Generate the model
		new ModelGenerator(model, graphMetaData).generateModel(context);
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
