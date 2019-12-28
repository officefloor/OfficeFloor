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
package net.officefloor.gef.activity.test;

import java.io.IOException;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.model.ActivityChanges;
import net.officefloor.activity.model.ActivityExceptionModel;
import net.officefloor.activity.model.ActivityInputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityModel.ActivityEvent;
import net.officefloor.activity.model.ActivityOutputModel;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivitySectionModel;
import net.officefloor.gef.activity.ActivityEditor;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.AbstractIdeTestApplication;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link ActivityEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityIdeTestApplication
		extends AbstractIdeTestApplication<ActivityModel, ActivityEvent, ActivityChanges> {

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	protected AbstractAdaptedIdeEditor<ActivityModel, ActivityEvent, ActivityChanges> createEditor(
			EnvironmentBridge envBridge) {
		return new ActivityEditor(envBridge);
	}

	@Override
	protected String getConfigurationFileName() {
		return "Test.activity.xml";
	}

	@Override
	protected String getReplaceConfigurationFileName() {
		return "Replace.activity.xml";
	}

	@Override
	public void init() throws Exception {
		this.register(ActivityInputModel.class, (model) -> {
			model.setActivityInputName("INPUT");
			model.setArgumentType(String.class.getName());
		});
		this.register(ActivityProcedureModel.class, (model) -> {
			model.setActivityProcedureName("Procedure");
			model.setResource(MockProcedure.class.getName());
			model.setSourceName(ClassProcedureSource.SOURCE_NAME);
			model.setProcedureName("procedure");
		});
		this.register(ActivitySectionModel.class, (model) -> {
			model.setActivitySectionName("Section");
			model.setSectionSourceClassName(ClassSectionSource.class.getName());
			model.setSectionLocation(MockSection.class.getName());
		});
		this.register(ActivityExceptionModel.class, (model) -> {
			model.setClassName(IOException.class.getName());
		});
		this.register(ActivityOutputModel.class, (model) -> {
			model.setActivityOutputName("OUTPUT");
		});
	}

}