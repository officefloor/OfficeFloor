/*-
 * #%L
 * net.officefloor.gef.editor.tests
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

package net.officefloor.eclipse.editor.test;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.officefloor.gef.common.structure.StructureLogger;
import net.officefloor.gef.editor.AdaptedBuilderContext;
import net.officefloor.gef.editor.AdaptedChildBuilder;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.model.impl.officefloor.OfficeFloorChangesImpl;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel.DeployedOfficeInputEvent;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeModel.DeployedOfficeEvent;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel.OfficeFloorManagedObjectSourceFlowEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel.OfficeFloorManagedObjectSourceInputDependencyEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel.OfficeFloorManagedObjectSourceTeamEvent;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorModel.OfficeFloorEvent;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel.OfficeFloorTeamEvent;

/**
 * Main for running example editor.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleOfficeFloorEditorMain extends AbstractEditorTestApplication<OfficeFloorModel> {

	/**
	 * Main to run the editor.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/*
	 * ============ AbstractEditorApplication ================
	 */

	@Override
	protected void buildModels(AdaptedBuilderContext builder) {

		// Specify the root model
		AdaptedRootBuilder<OfficeFloorModel, OfficeFloorChanges> root = builder.root(OfficeFloorModel.class,
				(r) -> new OfficeFloorChangesImpl(r));

		// Obtain the error handler
		AdaptedErrorHandler errorHandler = root.getErrorHandler();

		// Provide static overlay
		root.overlay(10, 10,
				(ctx) -> ctx.getOverlayParent().getChildren().add(new Label("Example editor for testing features")));

		// Provide log of structure
		root.overlay(300, 10, (ctx) -> {
			Button log = new Button("log");
			log.setOnAction((event) -> {
				root.getErrorHandler().isError(() -> StructureLogger.logFull(log, System.out));
			});
			ctx.getOverlayParent().getChildren().add(log);
		});

		// Provide styling of palette indicator
		Property<String> paletteIndicatorStyle = root.paletteIndicatorStyle();
		root.overlay(10, 200, (ctx) -> {
			Button style = new Button("palette indicator style");
			boolean[] toggle = new boolean[] { false };
			style.setOnAction((event) -> {
				toggle[0] = !toggle[0];
				String css = toggle[0] ? ".palette-indicator { -fx-background-color: green }" : "";
				System.out.println("Toggle style sheet: " + css);
				paletteIndicatorStyle.setValue(css);
			});
			ctx.getOverlayParent().getChildren().add(style);
		});

		// Provide styling of palette
		Property<String> paletteStyle = root.paletteStyle();
		root.overlay(10, 230, (ctx) -> {
			Button style = new Button("palette style");
			boolean[] toggle = new boolean[] { false };
			style.setOnAction((event) -> {
				toggle[0] = !toggle[0];
				String css = toggle[0] ? ".palette { -fx-background-color: cornsilk }" : "";
				System.out.println("Toggle style sheet: " + css);
				paletteStyle.setValue(css);
			});
			ctx.getOverlayParent().getChildren().add(style);
		});

		// Provide styling of editor
		Property<String> editorStyle = root.editorStyle();
		root.overlay(10, 260, (ctx) -> {
			Button style = new Button("editor style");
			boolean[] toggle = new boolean[] { false };
			style.setOnAction((event) -> {
				toggle[0] = !toggle[0];

				// Provide styling
				String css = toggle[0]
						? ".editor { -fx-background-color: green } .connection Path { -fx-stroke: white }"
						: "";
				System.out.println("Toggle style sheet: " + css);
				editorStyle.setValue(css);

				// Indicate whether to show grid
				root.getGridModel().setShowGrid(!toggle[0]);
			});
			ctx.getOverlayParent().getChildren().add(style);
		});

		// Managed Object Source
		AdaptedParentBuilder<OfficeFloorModel, OfficeFloorChanges, OfficeFloorManagedObjectSourceModel, OfficeFloorManagedObjectSourceEvent> mos = root
				.parent(new OfficeFloorManagedObjectSourceModel("Managed Object Source", null, null, null),
						(r) -> r.getOfficeFloorManagedObjectSources(), (model, context) -> {
							VBox container = new VBox();
							context.label(container);
							HBox dependenciesFlows = context.addNode(container, new HBox());
							context.addNode(dependenciesFlows, context.childGroup("dependencies", new VBox()));
							context.addNode(dependenciesFlows, context.childGroup("flows", new VBox()));
							context.addNode(container, context.childGroup("teams", new VBox()));
							return container;
						}, OfficeFloorEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE,
						OfficeFloorEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE);
		mos.label((m) -> m.getOfficeFloorManagedObjectSourceName(),
				OfficeFloorManagedObjectSourceEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_NAME);
		mos.create((p) -> {
			p.overlay((overlay) -> {
				Button button = new Button("Create MOS");
				button.setOnAction((event) -> {
					overlay.close();
					p.getRootModel().addOfficeFloorManagedObjectSource(
							p.position(new OfficeFloorManagedObjectSourceModel("Created Managed Object Source", null,
									null, null)));
				});
				overlay.getOverlayParent().getChildren().add(button);
			});
		});
		mos.action((ctx) -> {
			ctx.getModel().addOfficeFloorManagedObjectSourceFlow(
					new OfficeFloorManagedObjectSourceFlowModel("Added Flow", null));
			ctx.getModel()
					.addOfficeFloorManagedObjectSourceTeam(new OfficeFloorManagedObjectSourceTeamModel("Added Team"));
			ctx.getModel().addOfficeFloorManagedObjectSourceInputDependency(
					new OfficeFloorManagedObjectSourceInputDependencyModel("Added dependency", null));
		}, DefaultImages.EDIT);
		mos.action((ctx) -> {
			if (ctx.getModel().getOfficeFloorManagedObjectSourceFlows().size() > 0) {
				ctx.getModel().removeOfficeFloorManagedObjectSourceFlow(
						ctx.getModel().getOfficeFloorManagedObjectSourceFlows().get(0));
			}
			if (ctx.getModel().getOfficeFloorManagedObjectSourceTeams().size() > 0) {
				ctx.getModel().removeOfficeFloorManagedObjectSourceTeam(
						ctx.getModel().getOfficeFloorManagedObjectSourceTeams().get(0));
			}
			if (ctx.getModel().getOfficeFloorManagedObjectSourceInputDependencies().size() > 0) {
				ctx.getModel().removeOfficeFloorManagedObjectSourceInputDependency(
						ctx.getModel().getOfficeFloorManagedObjectSourceInputDependencies().get(0));
			}
		}, (visual) -> new Label("clean"));
		mos.action(
				(ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().removeOfficeFloorManagedObjectSource(ctx.getModel())),
				DefaultImages.DELETE);
		mos.action((ctx) -> {
			errorHandler.showError("Example error message");
		}, (visual) -> new Label("Error Message"));
		mos.action((ctx) -> {
			throw new Exception("Show up in error details");
		}, (visual) -> new Label("Exception"));

		// Managed Object Source Input Dependencies
		AdaptedChildBuilder<OfficeFloorModel, OfficeFloorChanges, OfficeFloorManagedObjectSourceInputDependencyModel, OfficeFloorManagedObjectSourceInputDependencyEvent> mosDependencies = mos
				.children("dependencies", (m) -> m.getOfficeFloorManagedObjectSourceInputDependencies(),
						OfficeFloorManagedObjectSourceEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY,
						OfficeFloorManagedObjectSourceEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY)
				.addChild(new OfficeFloorManagedObjectSourceInputDependencyModel("Prototype", null),
						(model, context) -> {
							HBox container = new HBox();
							context.label(container);
							return container;
						});
		mosDependencies.label((m) -> m.getOfficeFloorManagedObjectSourceInputDependencyName(),
				OfficeFloorManagedObjectSourceInputDependencyEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY_NAME);

		// Managed Object Source Flows
		AdaptedChildBuilder<OfficeFloorModel, OfficeFloorChanges, OfficeFloorManagedObjectSourceFlowModel, OfficeFloorManagedObjectSourceFlowEvent> mosFlows = mos
				.children("flows", (m) -> m.getOfficeFloorManagedObjectSourceFlows(),
						OfficeFloorManagedObjectSourceEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW,
						OfficeFloorManagedObjectSourceEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW)
				.addChild(new OfficeFloorManagedObjectSourceFlowModel("Prototype", null), (model, context) -> {
					HBox container = new HBox();
					context.label(container);
					context.addNode(container, context.action((ctx) -> {
						AdaptedModel<?> adapted = ctx.getAdaptedModel();
						while (adapted != null) {
							System.out.print(adapted.getModel().getClass().getSimpleName() + " ");
							adapted = adapted.getParent();
						}
						System.out.println();
					}, DefaultImages.ADD));
					context.addNode(
							container, context
									.connector(DefaultConnectors.FLOW,
											OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class)
									.getNode());
					return container;
				});
		mosFlows.label((m) -> m.getOfficeFloorManagedObjectSourceFlowName(),
				OfficeFloorManagedObjectSourceFlowEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW_NAME);
		mosFlows.connectOne(OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class,
				(s) -> s.getDeployedOfficeInput(), (c) -> c.getOfficeFloorManagedObjectSoruceFlow(),
				OfficeFloorManagedObjectSourceFlowEvent.CHANGE_DEPLOYED_OFFICE_INPUT)
				.toMany(DeployedOfficeInputModel.class, (t) -> t.getOfficeFloorManagedObjectSourceFlows(),
						(c) -> c.getDeployedOfficeInput(),
						DeployedOfficeInputEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW,
						DeployedOfficeInputEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW);

		// Managed Object Source Teams
		AdaptedChildBuilder<OfficeFloorModel, OfficeFloorChanges, OfficeFloorManagedObjectSourceTeamModel, OfficeFloorManagedObjectSourceTeamEvent> mosTeams = mos
				.children("teams", (m) -> m.getOfficeFloorManagedObjectSourceTeams(),
						OfficeFloorManagedObjectSourceEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM,
						OfficeFloorManagedObjectSourceEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM)
				.addChild(new OfficeFloorManagedObjectSourceTeamModel("Prototype", null), (model, context) -> {
					HBox container = new HBox();
					context.addNode(container, context.connector(DefaultConnectors.TEAM,
							OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class).getNode());
					context.label(container);
					return container;
				});
		mosTeams.label((m) -> m.getOfficeFloorManagedObjectSourceTeamName(),
				OfficeFloorManagedObjectSourceTeamEvent.CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM_NAME);
		mosTeams.connectOne(OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				(s) -> s.getOfficeFloorTeam(), (c) -> c.getOfficeFloorManagedObjectSourceTeam(),
				OfficeFloorManagedObjectSourceTeamEvent.CHANGE_OFFICE_FLOOR_TEAM)
				.toMany(OfficeFloorTeamModel.class, (t) -> t.getOfficeFloorManagedObjectSourceTeams(),
						(c) -> c.getOfficeFloorTeam(), OfficeFloorTeamEvent.ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM,
						OfficeFloorTeamEvent.REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM)
				.create((s, t, ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().linkOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(s, t)))
				.delete((ctx) -> ctx.getChangeExecutor().execute(
						ctx.getOperations().removeOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(ctx.getModel())));
		mosTeams.style().setValue("{ -fx-text-fill: blue }");

		// Team
		AdaptedParentBuilder<OfficeFloorModel, OfficeFloorChanges, OfficeFloorTeamModel, OfficeFloorTeamEvent> team = root
				.parent(new OfficeFloorTeamModel("Team", 50, null, false), (r) -> r.getOfficeFloorTeams(),
						(model, context) -> {
							HBox container = new HBox();
							context.label(container);
							context.addNode(
									container, context
											.connector(DefaultConnectors.TEAM,
													OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class)
											.getNode());
							return container;
						}, OfficeFloorEvent.ADD_OFFICE_FLOOR_TEAM, OfficeFloorEvent.REMOVE_OFFICE_FLOOR_TEAM);
		team.label((m) -> m.getOfficeFloorTeamName(), OfficeFloorTeamEvent.CHANGE_OFFICE_FLOOR_TEAM_NAME);
		team.create((p) -> p.getRootModel()
				.addOfficeFloorTeam(p.position(new OfficeFloorTeamModel("Created Team", 50, null, false))));
		team.action((ctx) -> ctx.getChangeExecutor().execute(ctx.getOperations().removeOfficeFloorTeam(ctx.getModel())),
				DefaultImages.DELETE);
		Runnable toggleTeamStyleRunnable = new Runnable() {

			private boolean toggle = false;

			@Override
			public void run() {
				String stylesheet = this.toggle ? ".parent { -fx-background-color: blue } { -fx-text-fill: white }"
						: "{ -fx-text-fill: green }";
				System.out.println("Toggle style sheet: " + stylesheet);
				team.style().setValue(stylesheet);
				this.toggle = !this.toggle;
			}
		};
		toggleTeamStyleRunnable.run();
		team.action((ctx) -> {
			toggleTeamStyleRunnable.run();
		}, (visual) -> new Label("Toggle style"));

		// Offices
		AdaptedParentBuilder<OfficeFloorModel, OfficeFloorChanges, DeployedOfficeModel, DeployedOfficeEvent> office = root
				.parent(new DeployedOfficeModel("Office", null, null), (r) -> r.getDeployedOffices(),
						(model, context) -> {
							VBox container = new VBox();
							context.label(container);
							context.addNode(container, context.childGroup("inputs", new VBox()));
							return container;
						}, OfficeFloorEvent.ADD_DEPLOYED_OFFICE, OfficeFloorEvent.REMOVE_DEPLOYED_OFFICE);
		office.label((m) -> m.getDeployedOfficeName(), DeployedOfficeEvent.CHANGE_DEPLOYED_OFFICE_NAME);

		// Office Input
		AdaptedChildBuilder<OfficeFloorModel, OfficeFloorChanges, DeployedOfficeInputModel, DeployedOfficeInputEvent> officeInput = office
				.children("inputs", (m) -> m.getDeployedOfficeInputs(), DeployedOfficeEvent.ADD_DEPLOYED_OFFICE_INPUT,
						DeployedOfficeEvent.REMOVE_DEPLOYED_OFFICE_INPUT)
				.addChild(new DeployedOfficeInputModel(), (model, context) -> {
					HBox container = new HBox();
					context.addNode(
							container, context
									.connector(DefaultConnectors.FLOW,
											OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class)
									.getNode());
					context.label(container);
					return container;
				});
		officeInput.label((m) -> m.getSectionName() + "." + m.getSectionInputName(),
				DeployedOfficeInputEvent.CHANGE_SECTION_NAME, DeployedOfficeInputEvent.CHANGE_SECTION_INPUT_NAME);
	}

	@Override
	protected OfficeFloorModel createRootModel() {

		// Create the OfficeFloor model
		OfficeFloorModel root = new OfficeFloorModel();

		// Managed Object Source
		OfficeFloorManagedObjectSourceModel mos = new OfficeFloorManagedObjectSourceModel("Managed Object Source",
				"net.example.ManagedObjectSource", "net.example.Type", "0", 250, 75);
		root.addOfficeFloorManagedObjectSource(mos);
		mos.addOfficeFloorManagedObjectSourceInputDependency(
				new OfficeFloorManagedObjectSourceInputDependencyModel("dependency", "net.example.Dependency"));
		OfficeFloorManagedObjectSourceFlowModel mosFlow = new OfficeFloorManagedObjectSourceFlowModel("flow",
				"net.example.Flow");
		mos.addOfficeFloorManagedObjectSourceFlow(mosFlow);
		OfficeFloorManagedObjectSourceTeamModel mosTeam = new OfficeFloorManagedObjectSourceTeamModel("Team");
		mos.addOfficeFloorManagedObjectSourceTeam(mosTeam);
		mos.addOfficeFloorManagedObjectSourceTeam(new OfficeFloorManagedObjectSourceTeamModel("Another"));

		// Team
		OfficeFloorTeamModel team = new OfficeFloorTeamModel("Team", 50, "net.example.TeamSource", false, 100, 50);
		root.addOfficeFloorTeam(team);

		// Another Team
		root.addOfficeFloorTeam(new OfficeFloorTeamModel("Team", 50, null, false, 100, 150));

		// Connect team
		OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel(
				team.getOfficeFloorTeamName(), mosTeam, team);
		mosTeamToTeam.connect();

		// Office
		DeployedOfficeModel office = new DeployedOfficeModel("Office", "net.example.OfficeSource", "location", 500,
				100);
		root.addDeployedOffice(office);
		DeployedOfficeInputModel officeInput = new DeployedOfficeInputModel("Section", "Input", null);
		office.addDeployedOfficeInput(officeInput);

		// Connect flow
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel mosFlowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel(
				office.getDeployedOfficeName(), officeInput.getSectionName(), officeInput.getSectionInputName(),
				mosFlow, officeInput);
		mosFlowToInput.connect();

		// Return the OfficeFloor model
		return root;
	}

	@Override
	protected OfficeFloorModel createRootReplacement() {

		// Create the OfficeFloor model
		OfficeFloorModel root = new OfficeFloorModel();

		// Provide replacement
		root.addDeployedOffice(
				new DeployedOfficeModel("Replacement", "net.example.OfficeSource", "location", 100, 100));

		// Return root model
		return root;
	}

}
