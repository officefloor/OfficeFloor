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

import java.util.Map;

import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionToActivityProcedureModel;
import net.officefloor.activity.model.ActivityInputToActivityProcedureModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.activity.model.ActivityProcedureModel.ActivityProcedureEvent;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureNextToActivityProcedureModel;
import net.officefloor.activity.model.ActivityProcedureOutputToActivityProcedureModel;
import net.officefloor.activity.model.ActivitySectionOutputToActivityProcedureModel;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.item.AbstractProcedureItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;

/**
 * Configuration of the {@link ActivityProcedureModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityProcedureItem extends
		AbstractProcedureItem<ActivityModel, ActivityEvent, ActivityChanges, ActivityProcedureModel, ActivityProcedureEvent, ActivityProcedureItem> {

	/*
	 * ========================= ActivityProcedureItem ==========================
	 */

	@Override
	public ActivityProcedureModel prototype() {
		return new ActivityProcedureModel("Procedure", null, null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getActivityProcedures(), ActivityEvent.ADD_ACTIVITY_PROCEDURE,
				ActivityEvent.REMOVE_ACTIVITY_PROCEDURE);
	}

	@Override
	public void loadToParent(ActivityModel parentModel, ActivityProcedureModel itemModel) {
		parentModel.addActivityProcedure(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivityProcedureName(),
				ActivityProcedureEvent.CHANGE_ACTIVITY_PROCEDURE_NAME);
	}

	@Override
	protected ActivityProcedureItem createItem() {
		return new ActivityProcedureItem();
	}

	@Override
	protected String getSectionName(ActivityProcedureModel model) {
		return model.getActivityProcedureName();
	}

	@Override
	protected String getResource(ActivityProcedureModel model) {
		return model.getResource();
	}

	@Override
	protected String getSourceName(ActivityProcedureModel model) {
		return model.getSourceName();
	}

	@Override
	protected String getProcedureName(ActivityProcedureModel model) {
		return model.getProcedureName();
	}

	@Override
	protected PropertyList getProcedureProperties(ActivityProcedureModel model) {
		return this.translateToPropertyList(model.getProperties(), p -> p.getName(), p -> p.getValue());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends ConnectionModel>[] getInputConnectionClasses() {
		return new Class[] { ActivityInputToActivityProcedureModel.class,
				ActivityExceptionToActivityProcedureModel.class, ActivityProcedureNextToActivityProcedureModel.class,
				ActivityProcedureOutputToActivityProcedureModel.class,
				ActivitySectionOutputToActivityProcedureModel.class };
	}

	@Override
	protected AbstractItem<ActivityModel, ActivityChanges, ActivityProcedureModel, ActivityProcedureEvent, ?, ?> createNextItem() {
		return new ActivityProcedureNextItem();
	}

	@Override
	protected AbstractItem<ActivityModel, ActivityChanges, ActivityProcedureModel, ActivityProcedureEvent, ?, ?> createOutputItem() {
		return new ActivityProcedureOutputItem();
	}

	@Override
	protected Change<ActivityProcedureModel> addProcedure(ActivityChanges operations, String name, String resource,
			String sourceName, String procedure, PropertyList properties, ProcedureType procedureType) {
		return operations.addProcedure(name, resource, sourceName, procedure, properties, procedureType);
	}

	@Override
	protected Change<ActivityProcedureModel> refactorProcedure(ActivityChanges operations, ActivityProcedureModel model,
			String name, String resource, String sourceName, String procedure, PropertyList properties,
			ProcedureType procedureType, Map<String, String> outputNameMapping) {
		return operations.refactorProcedure(model, name, resource, sourceName, procedure, properties, procedureType,
				outputNameMapping);
	}

	@Override
	protected Change<ActivityProcedureModel> removeProcedure(ActivityChanges operations, ActivityProcedureModel model) {
		return operations.removeProcedure(model);
	}

}
