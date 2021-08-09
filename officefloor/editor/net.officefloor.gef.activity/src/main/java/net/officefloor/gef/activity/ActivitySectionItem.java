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
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.activity.model.ActivitySectionModel.ActivitySectionEvent;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.gef.item.AbstractSectionItem;
import net.officefloor.model.change.Change;

/**
 * Configuration for the {@link ActivitySectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivitySectionItem extends
		AbstractSectionItem<ActivityModel, ActivityEvent, ActivityChanges, ActivitySectionModel, ActivitySectionEvent, ActivitySectionItem> {

	/*
	 * =========================== AbstractSectionItem =========================
	 */

	@Override
	public ActivitySectionModel prototype() {
		return new ActivitySectionModel("Section", null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getActivitySections(), ActivityEvent.ADD_ACTIVITY_SECTION,
				ActivityEvent.REMOVE_ACTIVITY_SECTION);
	}

	@Override
	public void loadToParent(ActivityModel parentModel, ActivitySectionModel itemModel) {
		parentModel.addActivitySection(itemModel);
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getActivitySectionName(),
				ActivitySectionEvent.CHANGE_ACTIVITY_SECTION_NAME);
	}

	@Override
	protected ActivitySectionItem createItem() {
		return new ActivitySectionItem();
	}

	@Override
	protected String getSectionName(ActivitySectionModel model) {
		return model.getActivitySectionName();
	}

	@Override
	protected String getSectionSourceClassName(ActivitySectionModel model) {
		return model.getSectionSourceClassName();
	}

	@Override
	protected String getSectionLocation(ActivitySectionModel model) {
		return model.getSectionLocation();
	}

	@Override
	protected PropertyList getSectionProperties(ActivitySectionModel model) {
		return this.translateToPropertyList(model.getProperties(), p -> p.getName(), p -> p.getValue());
	}

	@Override
	protected AbstractItem<ActivityModel, ActivityChanges, ActivitySectionModel, ActivitySectionEvent, ?, ?> createInputItem() {
		return new ActivitySectionInputItem();
	}

	@Override
	protected AbstractItem<ActivityModel, ActivityChanges, ActivitySectionModel, ActivitySectionEvent, ?, ?> createOutputItem() {
		return new ActivitySectionOutputItem();
	}

	@Override
	protected Change<ActivitySectionModel> addSection(ActivityChanges operations, String name, String sourceClassName,
			String location, PropertyList properties, SectionType sectionType) {
		return operations.addSection(name, sourceClassName, location, properties, sectionType);
	}

	@Override
	protected Change<ActivitySectionModel> refactorSection(ActivityChanges operations, ActivitySectionModel model,
			String name, String sourceClassName, String location, PropertyList properties, SectionType sectionType,
			Map<String, String> inputNameMapping, Map<String, String> outputNameMapping) {
		return operations.refactorSection(model, name, sourceClassName, location, properties, sectionType,
				inputNameMapping, outputNameMapping);
	}

	@Override
	protected Change<ActivitySectionModel> removeSection(ActivityChanges operations, ActivitySectionModel model) {
		return operations.removeSection(model);
	}

}
