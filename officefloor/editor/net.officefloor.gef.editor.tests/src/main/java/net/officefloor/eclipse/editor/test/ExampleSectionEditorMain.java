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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.officefloor.gef.common.structure.StructureLogger;
import net.officefloor.gef.editor.AdaptedBuilderContext;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * Main for running example editor.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleSectionEditorMain extends AbstractEditorTestApplication<SectionModel> {

	/**
	 * Main to run the editor.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/*
	 * ============ AbstractEditorMain ================
	 */

	@Override
	protected void buildModels(AdaptedBuilderContext builder) {

		// Specify the root model
		AdaptedRootBuilder<SectionModel, SectionChanges> root = builder.root(SectionModel.class,
				(r) -> new SectionChangesImpl(r));

		// Provide static overlay
		root.overlay(10, 10,
				(ctx) -> ctx.getOverlayParent().getChildren().add(new Label("Example editor for testing features")));

		// Provide log of structure
		root.overlay(10, 50, (ctx) -> {
			Button log = new Button("log");
			log.setOnAction((event) -> {
				root.getErrorHandler().isError(() -> StructureLogger.logFull(log, System.out));
			});
			ctx.getOverlayParent().getChildren().add(log);
		});

		// Function
		AdaptedParentBuilder<SectionModel, SectionChanges, FunctionModel, FunctionEvent> function = root.parent(
				new FunctionModel("Function", false, null, null, null), (r) -> r.getFunctions(), (model, context) -> {
					HBox container = new HBox();
					context.addNode(container, context.connector(DefaultConnectors.FLOW)
							.target(FunctionToNextFunctionModel.class).getNode());
					context.label(container);
					context.addNode(container, context.connector(DefaultConnectors.FLOW)
							.source(FunctionToNextFunctionModel.class).getNode());
					return container;
				}, SectionEvent.ADD_FUNCTION, SectionEvent.REMOVE_FUNCTION);
		function.label((m) -> m.getFunctionName(), FunctionEvent.CHANGE_FUNCTION_NAME);
		function.connectOne(FunctionToNextFunctionModel.class, (s) -> s.getNextFunction(),
				(c) -> c.getPreviousFunction(), FunctionEvent.CHANGE_NEXT_FUNCTION)
				.toMany(FunctionModel.class, (t) -> t.getPreviousFunctions(), (c) -> c.getNextFunction(),
						FunctionEvent.ADD_PREVIOUS_FUNCTION, FunctionEvent.REMOVE_PREVIOUS_FUNCTION)
				.create((s, t, ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().linkFunctionToNextFunction(s, t)))
				.delete((ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().removeFunctionToNextFunction(ctx.getModel())));
	}

	@Override
	protected SectionModel createRootModel() {

		// Create the Section model
		SectionModel root = new SectionModel();

		// Function One
		FunctionModel functionOne = new FunctionModel("Function One", false, null, null, null, 100, 100);
		root.addFunction(functionOne);

		// Function Two
		FunctionModel functionTwo = new FunctionModel("Function Two", false, null, null, null, 200, 200);
		root.addFunction(functionTwo);

		// Connect functions
		FunctionToNextFunctionModel connection = new FunctionToNextFunctionModel(functionTwo.getFunctionName(),
				functionOne, functionTwo);
		connection.connect();

		// Return the Section model
		return root;
	}

	@Override
	protected SectionModel createRootReplacement() {

		// Create the Section model
		SectionModel root = new SectionModel();

		// Provide replacement
		root.addFunction(new FunctionModel("Replaced", false, null, null, null, 150, 150));

		// Return the Section model
		return root;
	}

}
