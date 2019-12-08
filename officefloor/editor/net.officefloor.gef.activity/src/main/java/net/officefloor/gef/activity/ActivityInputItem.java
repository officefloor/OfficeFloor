/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.activity;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityInputModel;
import net.officefloor.activity.model.ActivityInputModel.ActivityInputEvent;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;

/**
 * Configuration of the {@link ActivityInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityInputItem extends
		AbstractConfigurableItem<ActivityModel, ActivityEvent, ActivityChanges, ActivityInputModel, ActivityInputEvent, ActivityInputItem> {

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Argument type.
	 */
	private String argumentType;

	/*
	 * ========================= AbstractConfigurableItem ========================
	 */

	@Override
	public ActivityInputModel prototype() {
		return new ActivityInputModel("Input", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getActivityInputs(), ActivityEvent.ADD_ACTIVITY_INPUT,
				ActivityEvent.REMOVE_ACTIVITY_INPUT);
	}

	@Override
	public Node visual(ActivityInputModel model, AdaptedChildVisualFactoryContext<ActivityInputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container, context.connector(DefaultConnectors.FLOW).target().getNode());
		return container;
	}

	@Override
	public void loadToParent(ActivityModel parentModel, ActivityInputModel itemModel) {
		parentModel.addActivityInput(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivityInputName(), ActivityInputEvent.CHANGE_ACTIVITY_INPUT_NAME);
	}

	@Override
	public ActivityInputItem item(ActivityInputModel model) {
		ActivityInputItem item = new ActivityInputItem();
		if (model != null) {
			item.name = model.getActivityInputName();
			item.argumentType = model.getArgumentType();
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(
				new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 50.0%, dodgerblue, lightskyblue)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "blue"));
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Input");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must specify name"))
					.setValue((item, value) -> item.name = value);
			builder.clazz("Argument Type").init((item) -> item.argumentType)
					.setValue((item, value) -> item.argumentType = value);

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addInput(item.name, item.argumentType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(
						context.getOperations().refactorInput(context.getModel(), item.name, item.argumentType));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeInput(context.getModel()));
		});
	}

}