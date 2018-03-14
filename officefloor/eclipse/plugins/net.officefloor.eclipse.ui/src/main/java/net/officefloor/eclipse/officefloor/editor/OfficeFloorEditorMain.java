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

import java.util.List;

import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.officefloor.eclipse.editor.AbstractEditorApplication;
import net.officefloor.eclipse.editor.AdaptedBuilderContext;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.Model;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel.OfficeFloorManagedObjectSourceFlowEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel.OfficeFloorManagedObjectSourceInputDependencyEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel.OfficeFloorManagedObjectSourceTeamEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel.OfficeFloorTeamEvent;

/**
 * Main for running {@link OfficeFloor} editor.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEditorMain extends AbstractEditorApplication {

	/**
	 * Main to run the editor.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/*
	 * ============ AbstractEditorMain ================
	 */

	@Override
	protected void buildModels(AdaptedBuilderContext builder) {

		// Managed Object Source
		AdaptedParentBuilder<OfficeFloorManagedObjectSourceModel, OfficeFloorManagedObjectSourceEvent> mos = builder
				.addParent(OfficeFloorManagedObjectSourceModel.class, (model, context) -> {
					VBox container = new VBox();
					context.label(container);
					HBox dependenciesFlows = context.addNode(container, new HBox());
					context.addNode(dependenciesFlows, context.childGroup("dependencies", new VBox()));
					context.addNode(dependenciesFlows, context.childGroup("flows", new VBox()));
					context.addNode(container, context.childGroup("teams", new HBox()));
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

		// Managed Object Source Teams
		AdaptedChildBuilder<OfficeFloorManagedObjectSourceTeamModel, OfficeFloorManagedObjectSourceTeamEvent> mosTeams = mos
				.children("teams", (m) -> m.getOfficeFloorManagedObjectSourceTeams(),
						OfficeFloorManagedObjectSourceEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM,
						OfficeFloorManagedObjectSourceEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM)
				.addChild(OfficeFloorManagedObjectSourceTeamModel.class, (model, context) -> {
					HBox container = new HBox();
					Label label = context.label(container);
					context.connect("team", label, OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class);
					return container;
				});
		mosTeams.label((m) -> m.getOfficeFloorManagedObjectSourceTeamName(),
				OfficeFloorManagedObjectSourceTeamEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM_NAME);
		mosTeams.connection(OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				(m) -> m.getOfficeFloorTeam(), OfficeFloorManagedObjectSourceTeamEvent.CHANGE_OFFICE_FLOOR_TEAM);

		// Team
		AdaptedParentBuilder<OfficeFloorTeamModel, OfficeFloorTeamEvent> team = builder
				.addParent(OfficeFloorTeamModel.class, (model, context) -> {
					VBox container = new VBox();
					Label label = context.label(container);
					context.connect("mos", label, OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class);
					return container;
				});
		team.label((m) -> m.getOfficeFloorTeamName(), OfficeFloorTeamEvent.CHANGE_OFFICE_FLOOR_TEAM_NAME);
		team.connections(OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				(m) -> m.getOfficeFloorManagedObjectSourceTeams(),
				OfficeFloorTeamEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM,
				OfficeFloorTeamEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM);
	}

	@Override
	protected void populateModels(List<Model> models) {

		// Managed Object Source
		OfficeFloorManagedObjectSourceModel mos = new OfficeFloorManagedObjectSourceModel("Managed Object Source",
				"net.example.ManagedObjectSource", "net.example.Type", "0", 10, 10);
		models.add(mos);
		mos.addOfficeFloorManagedObjectSourceInputDependency(
				new OfficeFloorManagedObjectSourceInputDependencyModel("dependency", "net.example.Dependency"));
		mos.addOfficeFloorManagedObjectSourceFlow(
				new OfficeFloorManagedObjectSourceFlowModel("flow", "net.example.Flow"));
		OfficeFloorManagedObjectSourceTeamModel mosTeam = new OfficeFloorManagedObjectSourceTeamModel("Team");
		mos.addOfficeFloorManagedObjectSourceTeam(mosTeam);

		// Team
		OfficeFloorTeamModel team = new OfficeFloorTeamModel("Team", "net.example.TeamSource", 10, 100);
		models.add(team);

		// Connection
		OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel(
				team.getOfficeFloorTeamName(), mosTeam, team);
		mosTeamToTeam.connect();
	}

}