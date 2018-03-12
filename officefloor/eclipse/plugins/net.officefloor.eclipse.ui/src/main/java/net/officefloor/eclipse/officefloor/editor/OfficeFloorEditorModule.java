/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.officefloor.editor;

import com.google.inject.Module;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.officefloor.eclipse.editor.AbstractEditorModule;
import net.officefloor.eclipse.editor.AdaptedBuilder;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel.OfficeFloorManagedObjectSourceFlowEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel.OfficeFloorManagedObjectSourceInputDependencyEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel.OfficeFloorTeamEvent;

/**
 * {@link Module} for the {@link OfficeFloor} editor.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEditorModule extends AbstractEditorModule {

	@Override
	protected void buildModels(AdaptedBuilder builder) {

		// Managed Object Source
		AdaptedParentBuilder<OfficeFloorManagedObjectSourceModel, OfficeFloorManagedObjectSourceEvent> mos = builder
				.addParent(OfficeFloorManagedObjectSourceModel.class, (model, context) -> {
					VBox container = new VBox();
					context.label(container);
					HBox dependenciesFlows = context.addNode(container, new HBox());
					VBox dependencies = context.addNode(dependenciesFlows, new VBox());
					context.childGroup("dependencies", dependencies);
					VBox flows = context.addNode(dependenciesFlows, new VBox());
					context.childGroup("flows", flows);
					return container;
				});
		mos.label((m) -> m.getOfficeFloorManagedObjectSourceName(),
				OfficeFloorManagedObjectSourceEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_NAME);

		// Managed Object Source Input Dependencies
		AdaptedChildBuilder<OfficeFloorManagedObjectSourceInputDependencyModel, OfficeFloorManagedObjectSourceInputDependencyEvent> mosDependencies = mos
				.children("dependencies", (m) -> m.getOfficeFloorManagedObjectSourceInputDependencies(),
						OfficeFloorManagedObjectSourceEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY,
						OfficeFloorManagedObjectSourceEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY)
				.addChild(OfficeFloorManagedObjectSourceInputDependencyModel.class, (model, context) -> {
					HBox container = new HBox();
					context.label(container);
					return container;
				});
		mosDependencies.label((m) -> m.getOfficeFloorManagedObjectSourceInputDependencyName(),
				OfficeFloorManagedObjectSourceInputDependencyEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY_NAME);

		// Managed Object Source Flows
		AdaptedChildBuilder<OfficeFloorManagedObjectSourceFlowModel, OfficeFloorManagedObjectSourceFlowEvent> mosFlows = mos
				.children("flows", (m) -> m.getOfficeFloorManagedObjectSourceFlows(),
						OfficeFloorManagedObjectSourceEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW,
						OfficeFloorManagedObjectSourceEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW)
				.addChild(OfficeFloorManagedObjectSourceFlowModel.class, (model, context) -> {
					HBox container = new HBox();
					context.label(container);
					return container;
				});
		mosFlows.label((m) -> m.getOfficeFloorManagedObjectSourceFlowName(),
				OfficeFloorManagedObjectSourceFlowEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW_NAME);

		// Team
		AdaptedParentBuilder<OfficeFloorTeamModel, OfficeFloorTeamEvent> team = builder
				.addParent(OfficeFloorTeamModel.class, (model, context) -> {
					VBox container = new VBox();
					context.label(container);
					return container;
				});
		team.label((m) -> m.getOfficeFloorTeamName(), OfficeFloorTeamEvent.CHANGE_OFFICE_FLOOR_TEAM_NAME);
	}

}