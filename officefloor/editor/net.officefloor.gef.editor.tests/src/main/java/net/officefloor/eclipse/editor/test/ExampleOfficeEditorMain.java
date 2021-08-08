/*-
 * #%L
 * net.officefloor.gef.editor.tests
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

package net.officefloor.eclipse.editor.test;

import org.eclipse.gef.geometry.planar.Dimension;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.gef.common.structure.StructureLogger;
import net.officefloor.gef.editor.AdaptedAreaBuilder;
import net.officefloor.gef.editor.AdaptedBuilderContext;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.editor.ParentToAreaConnectionModel;
import net.officefloor.model.impl.office.OfficeChangesImpl;
import net.officefloor.model.office.GovernanceAreaModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceModel.GovernanceEvent;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeModel.OfficeEvent;

/**
 * Main for running example editor.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleOfficeEditorMain extends AbstractEditorTestApplication<OfficeModel> {

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
		AdaptedRootBuilder<OfficeModel, OfficeChanges> root = builder.root(OfficeModel.class,
				(r) -> new OfficeChangesImpl(r));

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

		// Governance
		AdaptedParentBuilder<OfficeModel, OfficeChanges, GovernanceModel, GovernanceEvent> governance = root
				.parent(new GovernanceModel("Governance", null, false), (r) -> r.getGovernances(), (model, context) -> {
					HBox container = new HBox();
					context.label(container);
					context.addNode(container,
							context.connector(DefaultConnectors.OBJECT, ParentToAreaConnectionModel.class).getNode());
					return container;
				}, OfficeEvent.ADD_GOVERNANCE, OfficeEvent.REMOVE_GOVERNANCE);
		governance.create((p) -> {
			p.getChangeExecutor().execute(p.getOperations().addGovernance("Governance", null,
					OfficeFloorCompiler.newPropertyList(), false, null));
		});
		governance.label((g) -> g.getGovernanceName(), GovernanceEvent.CHANGE_GOVERNANCE_NAME);
		governance.action((context) -> {
			context.getChangeExecutor()
					.execute(context.getOperations().addGovernanceArea(context.getAdaptedModel().getModel(), 100, 100));
		}, DefaultImages.ADD);
		governance.action((context) -> {
			context.getChangeExecutor()
					.execute(context.getOperations().removeGovernance(context.getAdaptedModel().getModel()));
		}, DefaultImages.DELETE);

		// Governance Area
		AdaptedAreaBuilder<OfficeModel, OfficeChanges, GovernanceAreaModel, GovernanceEvent> area = governance.area(
				new GovernanceAreaModel(), (p) -> p.getGovernanceAreas(),
				(a) -> new Dimension(a.getWidth(), a.getHeight()), (a, dimension) -> {
					a.setWidth((int) dimension.getWidth());
					a.setHeight((int) dimension.getHeight());
				}, GovernanceEvent.ADD_GOVERNANCE_AREA, GovernanceEvent.REMOVE_GOVERNANCE_AREA);
		area.action((context) -> {
			context.getChangeExecutor()
					.execute(context.getOperations().removeGovernanceArea(context.getAdaptedModel().getModel()));
		}, DefaultImages.DELETE);
		Runnable toggleAreaStyleRunnable = new Runnable() {

			private boolean toggle = false;

			@Override
			public void run() {
				String stylesheet = this.toggle ? "{ -fx-fill: blue }" : "{ -fx-fill: green }";
				System.out.println("Toggle style sheet: " + stylesheet);
				area.style().setValue(stylesheet);
				this.toggle = !this.toggle;
			}
		};
		area.action((ctx) -> {
			toggleAreaStyleRunnable.run();
		}, (visual) -> new Label("Toggle style"));
	}

	@Override
	protected OfficeModel createRootModel() {

		// Create the Office model
		OfficeModel root = new OfficeModel();

		// Governance
		GovernanceModel governance = new GovernanceModel("Governance", null, false, 250, 75);
		root.addGovernance(governance);

		// Governance Area
		governance.addGovernanceArea(new GovernanceAreaModel(100, 600, 100, 150));

		// Return the Office model
		return root;
	}

	@Override
	protected OfficeModel createRootReplacement() {

		// Create the Office model
		OfficeModel root = new OfficeModel();

		// Add replacement model
		root.addGovernance(new GovernanceModel("Replaced", null, false, 100, 100));

		// Return the Office model
		return root;
	}

}
