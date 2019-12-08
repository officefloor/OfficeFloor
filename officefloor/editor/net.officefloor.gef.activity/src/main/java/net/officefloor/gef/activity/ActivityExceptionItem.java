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

import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionModel;
import net.officefloor.activity.model.ActivityExceptionModel.ActivityExceptionEvent;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
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
		return new Class[] {};
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