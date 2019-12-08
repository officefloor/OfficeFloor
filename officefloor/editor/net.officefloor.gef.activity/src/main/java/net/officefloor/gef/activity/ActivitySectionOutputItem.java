/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.activity.model.ActivitySectionModel.ActivitySectionEvent;
import net.officefloor.activity.model.ActivitySectionOutputModel;
import net.officefloor.activity.model.ActivitySectionOutputModel.ActivitySectionOutputEvent;
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
	public Pane visual(ActivitySectionOutputModel model, AdaptedChildVisualFactoryContext<ActivitySectionOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivitySectionOutputName(),
				ActivitySectionOutputEvent.CHANGE_ACTIVITY_SECTION_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
//		connections.add(new IdeConnection<>(ActivitySectionOutputToActivitySectionInputModel.class)
//				.connectOne(s -> s.getActivitySectionInput(), c -> c.getActivitySectionOutput(),
//						ActivitySectionOutputEvent.CHANGE_WOOF_SECTION_INPUT)
//				.to(ActivitySectionInputModel.class)
//				.many(t -> t.getActivitySectionOutputs(), c -> c.getActivitySectionInput(),
//						ActivitySectionInputEvent.ADD_WOOF_SECTION_OUTPUT, ActivitySectionInputEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSectionInput(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor()
//							.execute(ctx.getOperations().removeSectionOutputToSectionInput(ctx.getModel()));
//				}));

		// Procedure
//		connections.add(new IdeConnection<>(ActivitySectionOutputToActivityProcedureModel.class)
//				.connectOne(s -> s.getActivityProcedure(), c -> c.getActivitySectionOutput(),
//						ActivitySectionOutputEvent.CHANGE_WOOF_PROCEDURE)
//				.to(ActivityProcedureModel.class)
//				.many(t -> t.getActivitySectionOutputs(), c -> c.getActivityProcedure(),
//						ActivityProcedureEvent.ADD_WOOF_SECTION_OUTPUT, ActivityProcedureEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToProcedure(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToProcedure(ctx.getModel()));
//				}));

		// Resource
//		connections.add(new IdeConnection<>(ActivitySectionOutputToActivityResourceModel.class)
//				.connectOne(s -> s.getActivityResource(), c -> c.getActivitySectionOutput(),
//						ActivitySectionOutputEvent.CHANGE_WOOF_RESOURCE)
//				.to(ActivityResourceModel.class)
//				.many(t -> t.getActivitySectionOutputs(), c -> c.getActivityResource(),
//						ActivityResourceEvent.ADD_WOOF_SECTION_OUTPUT, ActivityResourceEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToResource(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToResource(ctx.getModel()));
//				}));
	}

}