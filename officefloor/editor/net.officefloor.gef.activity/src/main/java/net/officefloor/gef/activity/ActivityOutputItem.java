/*-
 * #%L
 * [bundle] Activity Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.activity;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionToActivityOutputModel;
import net.officefloor.activity.model.ActivityInputToActivityOutputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.activity.model.ActivityOutputModel;
import net.officefloor.activity.model.ActivityOutputModel.ActivityOutputEvent;
import net.officefloor.activity.model.ActivityProcedureNextToActivityOutputModel;
import net.officefloor.activity.model.ActivityProcedureOutputToActivityOutputModel;
import net.officefloor.activity.model.ActivitySectionOutputToActivityOutputModel;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;

/**
 * Configuration of the {@link ActivityOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityOutputItem extends
		AbstractConfigurableItem<ActivityModel, ActivityEvent, ActivityChanges, ActivityOutputModel, ActivityOutputEvent, ActivityOutputItem> {

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Parameter type.
	 */
	private String parameterType;

	/*
	 * ======================= AbstractConfigurableItem =========================
	 */

	@Override
	public ActivityOutputModel prototype() {
		return new ActivityOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getActivityOutputs(), ActivityEvent.ADD_ACTIVITY_OUTPUT,
				ActivityEvent.REMOVE_ACTIVITY_OUTPUT);
	}

	@Override
	public Node visual(ActivityOutputModel model, AdaptedChildVisualFactoryContext<ActivityOutputModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, ActivityInputToActivityOutputModel.class,
						ActivityExceptionToActivityOutputModel.class, ActivityProcedureNextToActivityOutputModel.class,
						ActivityProcedureOutputToActivityOutputModel.class,
						ActivitySectionOutputToActivityOutputModel.class).target().getNode());
		context.label(container);
		return container;
	}

	@Override
	public void loadToParent(ActivityModel parentModel, ActivityOutputModel itemModel) {
		parentModel.addActivityOutput(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivityOutputName(),
				ActivityOutputEvent.CHANGE_ACTIVITY_OUTPUT_NAME);
	}

	@Override
	public ActivityOutputItem item(ActivityOutputModel model) {
		ActivityOutputItem item = new ActivityOutputItem();
		if (model != null) {
			item.name = model.getActivityOutputName();
			item.parameterType = model.getParameterType();
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
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Output");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must specify name"))
					.setValue((item, value) -> item.name = value);
			builder.clazz("Parameter Type").init((item) -> item.parameterType)
					.setValue((item, value) -> item.parameterType = value);

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addOutput(item.name, item.parameterType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(
						context.getOperations().refactorOutput(context.getModel(), item.name, item.parameterType));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeOutput(context.getModel()));
		});
	}

}
