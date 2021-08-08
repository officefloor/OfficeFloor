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
