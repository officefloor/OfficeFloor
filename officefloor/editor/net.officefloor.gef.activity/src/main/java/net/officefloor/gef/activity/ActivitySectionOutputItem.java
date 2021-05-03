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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityOutputModel;
import net.officefloor.activity.model.ActivityOutputModel.ActivityOutputEvent;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureModel.ActivityProcedureEvent;
import net.officefloor.activity.model.ActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel.ActivitySectionInputEvent;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.activity.model.ActivitySectionModel.ActivitySectionEvent;
import net.officefloor.activity.model.ActivitySectionOutputModel;
import net.officefloor.activity.model.ActivitySectionOutputModel.ActivitySectionOutputEvent;
import net.officefloor.activity.model.ActivitySectionOutputToActivityOutputModel;
import net.officefloor.activity.model.ActivitySectionOutputToActivityProcedureModel;
import net.officefloor.activity.model.ActivitySectionOutputToActivitySectionInputModel;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;

/**
 * Configuration for the {@link ActivitySectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivitySectionOutputItem extends
		AbstractItem<ActivityModel, ActivityChanges, ActivitySectionModel, ActivitySectionEvent, ActivitySectionOutputModel, ActivitySectionOutputEvent> {

	@Override
	public ActivitySectionOutputModel prototype() {
		return new ActivitySectionOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), ActivitySectionEvent.ADD_OUTPUT,
				ActivitySectionEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(ActivitySectionModel parentModel, ActivitySectionOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(ActivitySectionOutputModel model,
			AdaptedChildVisualFactoryContext<ActivitySectionOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, ActivitySectionOutputToActivityProcedureModel.class,
						ActivitySectionOutputToActivitySectionInputModel.class,
						ActivitySectionOutputToActivityOutputModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivitySectionOutputName(),
				ActivitySectionOutputEvent.CHANGE_ACTIVITY_SECTION_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Procedure
		connections.add(new IdeConnection<>(ActivitySectionOutputToActivityProcedureModel.class)
				.connectOne(s -> s.getActivityProcedure(), c -> c.getActivitySectionOutput(),
						ActivitySectionOutputEvent.CHANGE_ACTIVITY_PROCEDURE)
				.to(ActivityProcedureModel.class)
				.many(t -> t.getActivitySectionOutputs(), c -> c.getActivityProcedure(),
						ActivityProcedureEvent.ADD_ACTIVITY_SECTION_OUTPUT,
						ActivityProcedureEvent.REMOVE_ACTIVITY_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToProcedure(ctx.getModel()));
				}));

		// Section Input
		connections
				.add(new IdeConnection<>(ActivitySectionOutputToActivitySectionInputModel.class)
						.connectOne(s -> s.getActivitySectionInput(), c -> c.getActivitySectionOutput(),
								ActivitySectionOutputEvent.CHANGE_ACTIVITY_SECTION_INPUT)
						.to(ActivitySectionInputModel.class)
						.many(t -> t.getActivitySectionOutputs(), c -> c.getActivitySectionInput(),
								ActivitySectionInputEvent.ADD_ACTIVITY_SECTION_OUTPUT,
								ActivitySectionInputEvent.REMOVE_ACTIVITY_SECTION_OUTPUT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSectionInput(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().removeSectionOutputToSectionInput(ctx.getModel()));
						}));

		// Output
		connections.add(new IdeConnection<>(ActivitySectionOutputToActivityOutputModel.class)
				.connectOne(s -> s.getActivityOutput(), c -> c.getActivitySectionOutput(),
						ActivitySectionOutputEvent.CHANGE_ACTIVITY_OUTPUT)
				.to(ActivityOutputModel.class)
				.many(t -> t.getActivitySectionOutputs(), c -> c.getActivityOutput(),
						ActivityOutputEvent.ADD_ACTIVITY_SECTION_OUTPUT,
						ActivityOutputEvent.REMOVE_ACTIVITY_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToOutput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToOutput(ctx.getModel()));
				}));
	}

}
