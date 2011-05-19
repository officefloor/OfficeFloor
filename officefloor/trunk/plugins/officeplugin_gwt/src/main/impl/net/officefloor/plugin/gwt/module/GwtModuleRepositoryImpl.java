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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

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
		Charset defaultCharset = Charset.defaultCharset();
		ByteArrayInputStream templateConfiguration = new ByteArrayInputStream(
				template.getBytes(defaultCharset));
		configuration.setConfiguration(templateConfiguration);
	}

	@Override
	public void updateGwtModule(GwtModuleModel module,
			ConfigurationItem configuration) throws Exception {

		// Only want to change the module configuration and leave rest as is.
		// Therefore loading DOM to be changed and written back.
		

		// TODO implement GwtModuleRepository.updateGwtModule
		throw new UnsupportedOperationException(
				"TODO implement GwtModuleRepository.updateGwtModule");
	}

}