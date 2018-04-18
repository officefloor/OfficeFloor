/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.section;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
import org.eclipse.swt.widgets.Shell;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * Configuration for {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalFlowConfiguration extends AbstractConfigurerRunnable {

	/**
	 * Test configuration.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		new ExternalFlowConfiguration().run();
	}

	/*
	 * =============== AbstractConfigurerRunnable =============
	 */

	@Override
	protected void loadConfiguration(Shell shell) {
		Configurer<ExternalFlowConfiguration> configurer = new Configurer<>(null, shell);
		this.loadAddConfiguration(configurer, new SectionChangesImpl(new SectionModel()), new ChangeExecutor() {

			@Override
			public void execute(ITransactionalOperation operation) {
				throw new IllegalStateException("Should not execute operation");
			}

			@Override
			public void execute(Change<?> change) {
				change.apply();
			}
		}, OfficeFloorOsgiBridge.getClassLoaderInstance());

	}

	/**
	 * Loads configuration for adding an {@link ExternalFlowModel}.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 */
	public void loadAddConfiguration(ConfigurationBuilder<ExternalFlowConfiguration> builder, SectionChanges changes,
			ChangeExecutor executor, OfficeFloorOsgiBridge compiler) {

		// Configure
		builder.title("External Flow");

		// Configure the name
		builder.text("Name").setValue((model, value) -> model.name = value)
				.validate(ValueValidator.notEmptyString("Must specify name"));

		// Configure optional argument type
		builder.clazz("Argument").setValue((model, value) -> model.argumentType = value);

		// Apply change
		builder.apply("Add", (model) -> {

			// Create the change
			Change<ExternalFlowModel> change = changes.addExternalFlow(model.name, model.argumentType);

			// Undertake the change
			executor.execute(change);
		});
	}

	/**
	 * Name of the {@link DeployedOffice}.
	 */
	private String name;

	/**
	 * Argument type.
	 */
	private String argumentType;

}