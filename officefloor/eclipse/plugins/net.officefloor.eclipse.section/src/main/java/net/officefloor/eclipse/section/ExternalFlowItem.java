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

import java.util.List;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
import org.eclipse.swt.widgets.Shell;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.ide.editor.AbstractParentConfigurableItem;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Configuration for {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalFlowItem extends
		AbstractParentConfigurableItem<SectionModel, SectionEvent, SectionChanges, ExternalFlowModel, ExternalFlowEvent, ExternalFlowItem> {

	/**
	 * Test configuration.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		new ExternalFlowItem().run();
	}

	/*
	 * =============== AbstractConfigurerRunnable =============
	 */

	@Override
	protected void loadConfiguration(Shell shell) {
		Configurer<ExternalFlowItem> configurer = new Configurer<>(OfficeFloorOsgiBridge.getClassLoaderInstance(),
				shell);
		this.loadAddConfiguration(configurer, new SectionChangesImpl(new SectionModel()), new ChangeExecutor() {

			@Override
			public void execute(ITransactionalOperation operation) {
				throw new IllegalStateException("Should not execute operation");
			}

			@Override
			public void execute(Change<?> change) {
				change.apply();
			}
		});
		configurer.loadConfiguration(this, shell);
	}

	/**
	 * Loads configuration for adding an {@link ExternalFlowModel}.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 */
	public void loadAddConfiguration(ConfigurationBuilder<ExternalFlowItem> builder, SectionChanges changes,
			ChangeExecutor executor) {

		// Configure
		builder.title("External Flow");

		// Configure the name
		builder.text("Name").setValue((model, value) -> model.name = value)
				.validate(ValueValidator.notEmptyString("Must specify name"));

		// Configure optional argument type
		builder.clazz("Argument").setValue((model, value) -> model.argumentType = value);

		// Apply change
		builder.apply("Add", (model) -> executor.execute(changes.addExternalFlow(model.name, model.argumentType)));
	}

	/**
	 * Name of the {@link DeployedOffice}.
	 */
	private String name;

	/**
	 * Argument type.
	 */
	private String argumentType;

	/*
	 * ================= AbstractParentConfigurableItem ============
	 */

	@Override
	protected ExternalFlowModel createPrototype() {
		return new ExternalFlowModel("External Flow", null);
	}

	@Override
	protected List<ExternalFlowModel> getModels(SectionModel parentModel) {
		return parentModel.getExternalFlows();
	}

	@Override
	protected SectionEvent[] rootChangeEvents() {
		return new SectionEvent[] { SectionEvent.ADD_EXTERNAL_FLOW, SectionEvent.REMOVE_EXTERNAL_FLOW };
	}

	@Override
	protected Pane createVisual(ExternalFlowModel model, AdaptedModelVisualFactoryContext context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	protected ExternalFlowEvent[] changeEvents() {
		return new ExternalFlowEvent[] { ExternalFlowEvent.CHANGE_EXTERNAL_FLOW_NAME };
	}

	@Override
	protected String getLabel(ExternalFlowModel model) {
		return model.getExternalFlowName();
	}

	@Override
	protected ExternalFlowItem createItem(ExternalFlowModel model) {
		ExternalFlowItem item = new ExternalFlowItem();
		if (model != null) {
			item.name = model.getExternalFlowName();
			item.argumentType = model.getArgumentType();
		}
		return item;
	}

	@Override
	protected void loadCommonConfiguration(ConfigurationBuilder<ExternalFlowItem> builder,
			ConfigurableModelContext<SectionChanges, ExternalFlowModel> context) {

		// Configure
		builder.title("External Flow");

		// Configure the name
		builder.text("Name").setValue((model, value) -> model.name = value)
				.validate(ValueValidator.notEmptyString("Must specify name"));

		// Configure optional argument type
		builder.clazz("Argument").setValue((model, value) -> model.argumentType = value);
	}

	@Override
	protected void loadAddConfiguration(ConfigurationBuilder<ExternalFlowItem> builder,
			ConfigurableModelContext<SectionChanges, ExternalFlowModel> context) {
		builder.apply("Add",
				(item) -> context.execute(context.getOperations().addExternalFlow(item.name, item.argumentType)));
	}

	@Override
	protected void loadRefactorConfiguration(ConfigurationBuilder<ExternalFlowItem> builder,
			ConfigurableModelContext<SectionChanges, ExternalFlowModel> context) {
		builder.apply("Refactor", (item) -> {
			// TODO implement refactor of external flow
			throw new UnsupportedOperationException("TODO renameExternalFlow to be refactorExternalFlow");
		});
	}

	@Override
	protected void deleteModel(ConfigurableModelContext<SectionChanges, ExternalFlowModel> context) {
		context.getOperations().removeExternalFlow(context.getModel());
	}

}