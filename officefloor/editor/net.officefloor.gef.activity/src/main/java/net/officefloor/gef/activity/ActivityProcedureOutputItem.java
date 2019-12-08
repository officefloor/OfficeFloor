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
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureModel.ActivityProcedureEvent;
import net.officefloor.activity.model.ActivityProcedureOutputModel;
import net.officefloor.activity.model.ActivityProcedureOutputModel.ActivityProcedureOutputEvent;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;

/**
 * Configuration for the {@link ActivityProcedureOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityProcedureOutputItem extends
		AbstractItem<ActivityModel, ActivityChanges, ActivityProcedureModel, ActivityProcedureEvent, ActivityProcedureOutputModel, ActivityProcedureOutputEvent> {

	@Override
	public ActivityProcedureOutputModel prototype() {
		return new ActivityProcedureOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), ActivityProcedureEvent.ADD_OUTPUT,
				ActivityProcedureEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(ActivityProcedureModel parentModel, ActivityProcedureOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(ActivityProcedureOutputModel model,
			AdaptedChildVisualFactoryContext<ActivityProcedureOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container, context.connector(DefaultConnectors.FLOW).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivityProcedureOutputName(),
				ActivityProcedureOutputEvent.CHANGE_ACTIVITY_PROCEDURE_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
//		connections
//				.add(new IdeConnection<>(ActivityProcedureOutputToActivitySectionInputModel.class)
//						.connectOne(s -> s.getActivitySectionInput(), c -> c.getActivityProcedureOutput(),
//								ActivityProcedureOutputEvent.CHANGE_WOOF_SECTION_INPUT)
//						.to(ActivitySectionInputModel.class)
//						.many(t -> t.getActivityProcedureOutputs(), c -> c.getActivitySectionInput(),
//								ActivitySectionInputEvent.ADD_WOOF_PROCEDURE_OUTPUT,
//								ActivitySectionInputEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
//						.create((s, t, ctx) -> {
//							ctx.getChangeExecutor()
//									.execute(ctx.getOperations().linkProcedureOutputToSectionInput(s, t));
//						}).delete((ctx) -> {
//							ctx.getChangeExecutor()
//									.execute(ctx.getOperations().removeProcedureOutputToSectionInput(ctx.getModel()));
//						}));


		// Procedure
//		connections.add(new IdeConnection<>(ActivityProcedureOutputToActivityProcedureModel.class)
//				.connectOne(s -> s.getActivityProcedure(), c -> c.getActivityProcedureOutput(),
//						ActivityProcedureOutputEvent.CHANGE_WOOF_PROCEDURE)
//				.to(ActivityProcedureModel.class)
//				.many(t -> t.getActivityProcedureOutputs(), c -> c.getActivityProcedure(),
//						ActivityProcedureEvent.ADD_WOOF_PROCEDURE_OUTPUT,
//						ActivityProcedureEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToProcedure(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor()
//							.execute(ctx.getOperations().removeProcedureOutputToProcedure(ctx.getModel()));
//				}));

		// Resource
//		connections.add(new IdeConnection<>(ActivityProcedureOutputToActivityResourceModel.class)
//				.connectOne(s -> s.getActivityResource(), c -> c.getActivityProcedureOutput(),
//						ActivityProcedureOutputEvent.CHANGE_WOOF_RESOURCE)
//				.to(ActivityResourceModel.class)
//				.many(t -> t.getActivityProcedureOutputs(), c -> c.getActivityResource(),
//						ActivityResourceEvent.ADD_WOOF_PROCEDURE_OUTPUT,
//						ActivityResourceEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToResource(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor()
//							.execute(ctx.getOperations().removeProcedureOutputToResource(ctx.getModel()));
//				}));
	}

}