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
package net.officefloor.gef.section.test;

import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.AbstractIdeTestApplication;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.section.FunctionNamespaceItem;
import net.officefloor.gef.section.ManagedObjectSourceItem;
import net.officefloor.gef.section.SectionEditor;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionIdeTestApplication extends AbstractIdeTestApplication<SectionModel, SectionEvent, SectionChanges> {

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	protected AbstractAdaptedIdeEditor<SectionModel, SectionEvent, SectionChanges> createEditor(
			EnvironmentBridge envBridge) {
		return new SectionEditor(envBridge);
	}

	@Override
	protected String getConfigurationFileName() {
		return "Test.section.xml";
	}

	@Override
	protected String getReplaceConfigurationFileName() {
		return "Replace.section.xml";
	}

	@Override
	public void init() throws Exception {
		this.register(ExternalFlowModel.class, (flow) -> {
			flow.setArgumentType(String.class.getName());
		});
		this.register(ExternalManagedObjectModel.class, (object) -> {
			object.setObjectType(String.class.getName());
		});
		this.register(FunctionNamespaceModel.class, (model) -> {
			model.setManagedFunctionSourceClassName(ClassManagedFunctionSource.class.getName());
			model.addProperty(new PropertyModel(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
					FunctionNamespaceItem.class.getName()));
			model.addManagedFunction(new ManagedFunctionModel("main"));
		});
		this.register(SectionManagedObjectModel.class, (model) -> {
			model.setSectionManagedObjectName("Managed Object");
		});
		this.register(SectionManagedObjectSourceModel.class, (model) -> {
			model.setManagedObjectSourceClassName(ClassManagedObjectSource.class.getName());
			model.addProperty(new PropertyModel(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					ManagedObjectSourceItem.class.getName()));
		});
	}

}