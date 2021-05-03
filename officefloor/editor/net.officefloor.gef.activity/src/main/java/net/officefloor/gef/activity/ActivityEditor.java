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
import net.officefloor.activity.model.ActivityChangesImpl;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.activity.model.ActivityRepository;
import net.officefloor.activity.model.ActivityRepositoryImpl;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;

/**
 * Activity Editor.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityEditor extends AbstractAdaptedIdeEditor<ActivityModel, ActivityEvent, ActivityChanges> {

	/**
	 * {@link ActivityRepository}.
	 */
	private static final ActivityRepository ACTIVITY_REPOSITORY = new ActivityRepositoryImpl(new ModelRepositoryImpl());

	/**
	 * Instantiate.
	 * 
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public ActivityEditor(EnvironmentBridge envBridge) {
		super(ActivityModel.class, (model) -> new ActivityChangesImpl(model), envBridge);
	}

	/*
	 * ================= AbstractIdeEditor ==================
	 */

	@Override
	public String fileName() {
		return "New.activity";
	}

	@Override
	public ActivityModel newFileRoot() {
		return new ActivityModel();
	}

	@Override
	public ActivityModel prototype() {
		return new ActivityModel();
	}

	@Override
	public String paletteStyle() {
		return ".palette { -fx-background-color: cornsilk }";
	}

	@Override
	public String paletteIndicatorStyle() {
		return ".palette-indicator { -fx-background-color: bisque }";
	}

	@Override
	public String editorStyle() {
		return ".connection Path { -fx-stroke: royalblue; -fx-opacity: 0.6 }";
	}

	@Override
	protected void loadParents(
			List<AbstractConfigurableItem<ActivityModel, ActivityEvent, ActivityChanges, ?, ?, ?>> parents) {
		parents.add(new ActivityInputItem());
		parents.add(new ActivityProcedureItem());
		parents.add(new ActivitySectionItem());
		parents.add(new ActivityExceptionItem());
		parents.add(new ActivityOutputItem());
	}

	@Override
	protected ActivityModel loadRootModel(ConfigurationItem configurationItem) throws Exception {
		ActivityModel activity = new ActivityModel();
		ACTIVITY_REPOSITORY.retrieveActivity(activity, configurationItem);
		return activity;
	}

	@Override
	public void saveRootModel(ActivityModel model, WritableConfigurationItem configurationItem) throws Exception {
		ACTIVITY_REPOSITORY.storeActivity(model, configurationItem);
	}

}
