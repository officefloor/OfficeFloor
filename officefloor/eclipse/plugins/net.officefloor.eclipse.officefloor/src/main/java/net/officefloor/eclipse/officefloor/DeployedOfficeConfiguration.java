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
package net.officefloor.eclipse.officefloor;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.impl.office.OfficeModelOfficeSource;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * Configuration for {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeployedOfficeConfiguration {

	/**
	 * Loads configuration for adding a {@link DeployedOfficeModel}.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 */
	public void loadAddConfiguration(ConfigurationBuilder<DeployedOfficeConfiguration> builder,
			ModelActionContext<OfficeFloorModel, OfficeFloorChanges, DeployedOfficeModel, ?> context,
			OfficeFloorOsgiBridge compiler) {

		// Configure the name
		builder.text("Name").setValue((model, value) -> model.name = value)
				.validate(ValueValidator.notEmptyString("Must specify name"));

		// Configure the OfficeFloor source
		Property<PropertyList> specificationProperty = new SimpleObjectProperty<>();
		builder.clazz("Source").setValue((model, value) -> model.officeFloorSource = value)
				.superType(OfficeSource.class).validate((event) -> {

					// Undertake validation
					ValueValidator.notEmptyString("Must specify " + OfficeFloorSource.class.getSimpleName())
							.validate(event);

					// TODO provide loadSpecification(String)

					// Update the specification
					PropertyList specification = compiler.getOfficeFloorCompiler().getOfficeLoader()
							.loadSpecification(OfficeModelOfficeSource.class);
					specificationProperty.setValue(specification);
				});

		// Configure the location
		builder.resource("Location").setValue((model, value) -> model.location = value);

		// Configure the properties
		builder.properties("Properties").specification(specificationProperty)
				.setValue((model, value) -> model.properties = value);

		// Validate by loading the type
		builder.validate((ctx) -> {

			// TODO provide loadOfficeFloorType(String)

			DeployedOfficeConfiguration model = ctx.getValue().getValue();
			model.officeType = compiler.getOfficeFloorCompiler().getOfficeLoader()
					.loadOfficeType(OfficeModelOfficeSource.class, model.location, model.properties);

		});

		// Apply change
		builder.apply("Add", (model) -> {
			context.getChangeExecutor().execute(context.getOperations().addDeployedOffice(model.name,
					model.officeFloorSource, model.location, model.properties, model.officeType));
		});

	}

	/**
	 * Name of the {@link DeployedOffice}.
	 */
	private String name;

	/**
	 * {@link OfficeFloorSource} {@link Class} name.
	 */
	private String officeFloorSource;

	/**
	 * Location of the {@link DeployedOffice}.
	 */
	private String location;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link OfficeType}.
	 */
	private OfficeType officeType;

}