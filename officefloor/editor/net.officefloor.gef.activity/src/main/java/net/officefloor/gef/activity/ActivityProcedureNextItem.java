/*-
 * #%L
 * [bundle] Activity Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.gef.activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityOutputModel;
import net.officefloor.activity.model.ActivityOutputModel.ActivityOutputEvent;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureModel.ActivityProcedureEvent;
import net.officefloor.activity.model.ActivityProcedureNextModel;
import net.officefloor.activity.model.ActivityProcedureNextModel.ActivityProcedureNextEvent;
import net.officefloor.activity.model.ActivityProcedureNextToActivityOutputModel;
import net.officefloor.activity.model.ActivityProcedureNextToActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureNextToActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel.ActivitySectionInputEvent;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;

/**
 * Configuration for the {@link ActivityProcedureNextModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityProcedureNextItem extends
		AbstractItem<ActivityModel, ActivityChanges, ActivityProcedureModel, ActivityProcedureEvent, ActivityProcedureNextModel, ActivityProcedureNextEvent> {

	@Override
	public ActivityProcedureNextModel prototype() {
		return new ActivityProcedureNextModel();
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> {
			ActivityProcedureNextModel next = parent.getNext();
			return next == null ? Collections.emptyList() : Arrays.asList(parent.getNext());
		}, ActivityProcedureEvent.CHANGE_NEXT);
	}

	@Override
	public void loadToParent(ActivityProcedureModel parentModel, ActivityProcedureNextModel itemModel) {
		parentModel.setNext(itemModel);
	}

	@Override
	public Pane visual(ActivityProcedureNextModel model,
			AdaptedChildVisualFactoryContext<ActivityProcedureNextModel> context) {
		VBox container = new VBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, ActivityProcedureNextToActivityProcedureModel.class,
						ActivityProcedureNextToActivitySectionInputModel.class,
						ActivityProcedureNextToActivityOutputModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> "");
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Procedure
		connections.add(new IdeConnection<>(ActivityProcedureNextToActivityProcedureModel.class)
				.connectOne(s -> s.getActivityProcedure(), c -> c.getActivityProcedureNext(),
						ActivityProcedureNextEvent.CHANGE_ACTIVITY_PROCEDURE)
				.to(ActivityProcedureModel.class)
				.many(t -> t.getActivityProcedureNexts(), c -> c.getActivityProcedure(),
						ActivityProcedureEvent.ADD_ACTIVITY_PROCEDURE_NEXT,
						ActivityProcedureEvent.REMOVE_ACTIVITY_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeProcedureNextToProcedure(ctx.getModel()));
				}));

		// Section Input
		connections
				.add(new IdeConnection<>(ActivityProcedureNextToActivitySectionInputModel.class)
						.connectOne(s -> s.getActivitySectionInput(), c -> c.getActivityProcedureNext(),
								ActivityProcedureNextEvent.CHANGE_ACTIVITY_SECTION_INPUT)
						.to(ActivitySectionInputModel.class)
						.many(t -> t.getActivityProcedureNexts(), c -> c.getActivitySectionInput(),
								ActivitySectionInputEvent.ADD_ACTIVITY_PROCEDURE_NEXT,
								ActivitySectionInputEvent.REMOVE_ACTIVITY_PROCEDURE_NEXT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToSectionInput(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().removeProcedureNextToSectionInput(ctx.getModel()));
						}));

		// Output
		connections.add(new IdeConnection<>(ActivityProcedureNextToActivityOutputModel.class)
				.connectOne(s -> s.getActivityOutput(), c -> c.getActivityProcedureNext(),
						ActivityProcedureNextEvent.CHANGE_ACTIVITY_OUTPUT)
				.to(ActivityOutputModel.class)
				.many(t -> t.getActivityProcedureNexts(), c -> c.getActivityOutput(),
						ActivityOutputEvent.ADD_ACTIVITY_PROCEDURE_NEXT,
						ActivityOutputEvent.REMOVE_ACTIVITY_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToOutput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeProcedureNextToOutput(ctx.getModel()));
				}));
	}

}
