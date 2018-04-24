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
import javafx.scene.layout.VBox;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Configuration for the {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, FunctionModel, FunctionEvent, FunctionItem> {

	/**
	 * Test configuration.
	 */
	public static void main(String[] args) {
		SectionEditor.launchConfigurer(new FunctionItem(), null);
	}

	/**
	 * Name.
	 */
	private String name;

	/*
	 * ============== AbstractConfigurableItem ===============
	 */

	@Override
	protected FunctionModel prototype() {
		return new FunctionModel("Function", false, null, null, null);
	}

	@Override
	protected FunctionItem item(FunctionModel model) {
		FunctionItem item = new FunctionItem();
		if (model != null) {
			item.name = model.getFunctionName();
		}
		return item;
	}

	@Override
	protected IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getFunctions(), SectionEvent.ADD_FUNCTION,
				SectionEvent.REMOVE_FUNCTION);
	}

	@Override
	protected Pane visual(FunctionModel model, AdaptedModelVisualFactoryContext<FunctionModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.addNode(heading, context.connector(ManagedFunctionToFunctionModel.class));
		context.label(heading);
		return container;
	}

	@Override
	protected IdeLabeller label() {
		return new IdeLabeller((model) -> model.getFunctionName(), FunctionEvent.CHANGE_FUNCTION_NAME);
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().refactor((builder, context) -> {
			builder.title("Function");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().renameFunction(context.getModel(), item.name));
			});
		}).delete((context) -> {
			context.execute(context.getOperations().removeFunction(context.getModel()));
		});
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
	}

	@Override
	protected void connections(List<IdeConnection<? extends ConnectionModel>> connections) {
	}

}