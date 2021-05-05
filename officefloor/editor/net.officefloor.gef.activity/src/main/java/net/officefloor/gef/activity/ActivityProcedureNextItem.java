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
