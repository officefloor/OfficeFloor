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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Configuration for {@link ExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalFlowItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, ExternalFlowModel, ExternalFlowEvent, ExternalFlowItem> {

	/**
	 * Test configuration.
	 */
	public static void main(String[] args) {
		SectionEditor.launchConfigurer(new ExternalFlowItem(), (flow) -> flow.setArgumentType(String.class.getName()));
	}

	/**
	 * Name.
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
	protected ExternalFlowModel prototype() {
		return new ExternalFlowModel("External Flow", null);
	}

	@Override
	protected IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getExternalFlows(), SectionEvent.ADD_EXTERNAL_FLOW,
				SectionEvent.REMOVE_EXTERNAL_FLOW);
	}

	@Override
	protected Pane visual(ExternalFlowModel model, AdaptedModelVisualFactoryContext<ExternalFlowModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	protected IdeLabeller label() {
		return new IdeLabeller((model) -> model.getExternalFlowName(), ExternalFlowEvent.CHANGE_EXTERNAL_FLOW_NAME);
	}

	@Override
	protected ExternalFlowItem item(ExternalFlowModel model) {
		ExternalFlowItem item = new ExternalFlowItem();
		if (model != null) {
			item.name = model.getExternalFlowName();
			item.argumentType = model.getArgumentType();
		}
		return item;
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("External Flow");
			builder.text("Name").init((model) -> model.name).setValue((model, value) -> model.name = value)
					.validate(ValueValidator.notEmptyString("Must specify name"));
			builder.clazz("Argument").init((model) -> model.argumentType)
					.setValue((model, value) -> model.argumentType = value);
		}).add((builder, context) -> {
			builder.apply("Add",
					(item) -> context.execute(context.getOperations().addExternalFlow(item.name, item.argumentType)));
		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				// TODO implement refactor of external flow
				throw new UnsupportedOperationException("TODO renameExternalFlow to be refactorExternalFlow");
			});
		}).delete((context) -> {
			context.execute(context.getOperations().removeExternalFlow(context.getModel()));
		});
	}

	@Override
	protected void children(List<IdeChildrenGroup> children) {
	}

	@Override
	protected void connections(List<IdeConnection<? extends ConnectionModel>> connections) {
	}

}