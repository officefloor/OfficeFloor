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

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityInputToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityProcedureNextToActivitySectionInputModel;
import net.officefloor.activity.model.ActivityProcedureOutputToActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionInputModel.ActivitySectionInputEvent;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.activity.model.ActivitySectionOutputToActivitySectionInputModel;
import net.officefloor.activity.model.ActivitySectionModel.ActivitySectionEvent;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;

/**
 * Configuration for the {@link ActivitySectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivitySectionInputItem extends
		AbstractItem<ActivityModel, ActivityChanges, ActivitySectionModel, ActivitySectionEvent, ActivitySectionInputModel, ActivitySectionInputEvent> {

	@Override
	public ActivitySectionInputModel prototype() {
		return new ActivitySectionInputModel("Input", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getInputs(), ActivitySectionEvent.ADD_INPUT,
				ActivitySectionEvent.REMOVE_INPUT);
	}

	@Override
	public void loadToParent(ActivitySectionModel parentModel, ActivitySectionInputModel itemModel) {
		parentModel.addInput(itemModel);
	}

	@Override
	public Pane visual(ActivitySectionInputModel model,
			AdaptedChildVisualFactoryContext<ActivitySectionInputModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, ActivityInputToActivitySectionInputModel.class,
						ActivityExceptionToActivitySectionInputModel.class,
						ActivityProcedureNextToActivitySectionInputModel.class,
						ActivityProcedureOutputToActivitySectionInputModel.class,
						ActivitySectionOutputToActivitySectionInputModel.class).getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivitySectionInputName(),
				ActivitySectionInputEvent.CHANGE_ACTIVITY_SECTION_INPUT_NAME);
	}

}
