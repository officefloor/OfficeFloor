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

import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionModel;
import net.officefloor.activity.model.ActivityExceptionModel.ActivityExceptionEvent;
import net.officefloor.activity.model.ActivityExceptionToActivityOutputModel;
import net.officefloor.activity.model.ActivityExceptionToActivityProcedureModel;
import net.officefloor.activity.model.ActivityExceptionToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.activity.model.ActivityOutputModel;
import net.officefloor.activity.model.ActivityOutputModel.ActivityOutputEvent;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureModel.ActivityProcedureEvent;
import net.officefloor.activity.model.ActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel.ActivitySectionInputEvent;
import net.officefloor.gef.item.AbstractExceptionItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;

/**
 * Configuration for the {@link ActivityExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityExceptionItem extends
		AbstractExceptionItem<ActivityModel, ActivityEvent, ActivityChanges, ActivityExceptionModel, ActivityExceptionEvent, ActivityExceptionItem> {

	/*
	 * ======================= AbstractExceptionItem =====================
	 */

	@Override
	public ActivityExceptionModel prototype() {
		return new ActivityExceptionModel("Exception");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getActivityExceptions(), ActivityEvent.ADD_ACTIVITY_EXCEPTION,
				ActivityEvent.REMOVE_ACTIVITY_EXCEPTION);
	}

	@Override
	public void loadToParent(ActivityModel parentModel, ActivityExceptionModel itemModel) {
		parentModel.addActivityException(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getClassName(), ActivityExceptionEvent.CHANGE_CLASS_NAME);
	}

	@Override
	protected ActivityExceptionItem createItem() {
		return new ActivityExceptionItem();
	}

	@Override
	protected String getExceptionClassName(ActivityExceptionModel model) {
		return model.getClassName();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends ConnectionModel>[] getInputConnectionClasses() {
		return new Class[] { ActivityExceptionToActivityProcedureModel.class,
				ActivityExceptionToActivitySectionInputModel.class, ActivityExceptionToActivityOutputModel.class };
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Procedure
		connections.add(new IdeConnection<>(ActivityExceptionToActivityProcedureModel.class)
				.connectOne(s -> s.getActivityProcedure(), c -> c.getActivityException(),
						ActivityExceptionEvent.CHANGE_ACTIVITY_PROCEDURE)
				.to(ActivityProcedureModel.class)
				.many(t -> t.getActivityExceptions(), c -> c.getActivityProcedure(),
						ActivityProcedureEvent.ADD_ACTIVITY_EXCEPTION, ActivityProcedureEvent.REMOVE_ACTIVITY_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeExceptionToProcedure(ctx.getModel()));
				}));

		// Section Input
		connections
				.add(new IdeConnection<>(ActivityExceptionToActivitySectionInputModel.class)
						.connectOne(s -> s.getActivitySectionInput(), c -> c.getActivityException(),
								ActivityExceptionEvent.CHANGE_ACTIVITY_SECTION_INPUT)
						.to(ActivitySectionInputModel.class)
						.many(t -> t.getActivityExceptions(), c -> c.getActivitySectionInput(),
								ActivitySectionInputEvent.ADD_ACTIVITY_EXCEPTION,
								ActivitySectionInputEvent.REMOVE_ACTIVITY_EXCEPTION)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToSectionInput(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().removeExceptionToSectionInput(ctx.getModel()));
						}));

		// Output
		connections.add(new IdeConnection<>(ActivityExceptionToActivityOutputModel.class)
				.connectOne(s -> s.getActivityOutput(), c -> c.getActivityException(),
						ActivityExceptionEvent.CHANGE_ACTIVITY_OUTPUT)
				.to(ActivityOutputModel.class)
				.many(t -> t.getActivityExceptions(), c -> c.getActivityOutput(),
						ActivityOutputEvent.ADD_ACTIVITY_EXCEPTION, ActivityOutputEvent.REMOVE_ACTIVITY_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToOutput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeExceptionToOutput(ctx.getModel()));
				}));
	}

	@Override
	protected Change<ActivityExceptionModel> addException(ActivityChanges operations, String exceptionClassName) {
		return operations.addException(exceptionClassName);
	}

	@Override
	protected Change<ActivityExceptionModel> refactorException(ActivityChanges operations, ActivityExceptionModel model,
			String exceptionClassName) {
		return operations.refactorException(model, exceptionClassName);
	}

	@Override
	protected Change<ActivityExceptionModel> removeException(ActivityChanges operations, ActivityExceptionModel model) {
		return operations.removeException(model);
	}

}
