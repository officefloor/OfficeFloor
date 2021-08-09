/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.section;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;
import net.officefloor.model.section.SubSectionOutputToFunctionModel;

/**
 * Configuration for the {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionItem extends
		AbstractConfigurableItem<SectionModel, SectionEvent, SectionChanges, FunctionModel, FunctionEvent, FunctionItem> {

	/**
	 * Name.
	 */
	private String name;

	/*
	 * ============== AbstractConfigurableItem ===============
	 */

	@Override
	public FunctionModel prototype() {
		return new FunctionModel("Function", false, null, null, null);
	}

	@Override
	public FunctionItem item(FunctionModel model) {
		FunctionItem item = new FunctionItem();
		if (model != null) {
			item.name = model.getFunctionName();
		}
		return item;
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getFunctions(), SectionEvent.ADD_FUNCTION,
				SectionEvent.REMOVE_FUNCTION);
	}

	@Override
	public void loadToParent(SectionModel parentModel, FunctionModel itemModel) {
		parentModel.addFunction(itemModel);
	}

	@Override
	public Pane visual(FunctionModel model, AdaptedChildVisualFactoryContext<FunctionModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.addNode(heading,
				context.connector(DefaultConnectors.FLOW, ManagedFunctionToFunctionModel.class,
						SubSectionOutputToFunctionModel.class, FunctionFlowToFunctionModel.class,
						FunctionEscalationToFunctionModel.class).target(FunctionToNextFunctionModel.class).getNode());
		context.label(heading);
		context.addNode(
				heading, context
						.connector(DefaultConnectors.FLOW, FunctionToNextExternalFlowModel.class,
								FunctionToNextSubSectionInputModel.class)
						.source(FunctionToNextFunctionModel.class).getNode());
		context.addNode(container, context.childGroup("outputs", new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getFunctionName(), FunctionEvent.CHANGE_FUNCTION_NAME);
	}

	@Override
	public String style() {
		return new IdeStyle().rule("-fx-background-color", "lightblue").toString();
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().refactor((builder, context) -> {
			builder.title("Function");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().renameFunction(context.getModel(), item.name));
			});
		}).delete((context) -> {
			context.execute(context.getOperations().removeFunction(context.getModel()));
		});
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup("outputs", new FunctionFlowItem(), new FunctionEscalationItem()));
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// External flow
		connections.add(new IdeConnection<>(FunctionToNextExternalFlowModel.class)
				.connectOne((s) -> s.getNextExternalFlow(), (c) -> c.getPreviousFunction(),
						FunctionEvent.CHANGE_NEXT_EXTERNAL_FLOW)
				.to(ExternalFlowModel.class)
				.many((t) -> t.getPreviousFunctions(), (c) -> c.getNextExternalFlow(),
						ExternalFlowEvent.ADD_PREVIOUS_FUNCTION, ExternalFlowEvent.REMOVE_PREVIOUS_FUNCTION)
				.create((s, t, ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().linkFunctionToNextExternalFlow(s, t)))
				.delete((ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().removeFunctionToNextExternalFlow(ctx.getModel()))));

		// Next function
		connections.add(new IdeConnection<>(FunctionToNextFunctionModel.class)
				.connectOne((s) -> s.getNextFunction(), (c) -> c.getPreviousFunction(),
						FunctionEvent.CHANGE_NEXT_FUNCTION)
				.to(FunctionModel.class)
				.many((t) -> t.getPreviousFunctions(), (c) -> c.getNextFunction(), FunctionEvent.ADD_PREVIOUS_FUNCTION,
						FunctionEvent.REMOVE_PREVIOUS_FUNCTION)
				.create((s, t, ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().linkFunctionToNextFunction(s, t)))
				.delete((ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().removeFunctionToNextFunction(ctx.getModel()))));

		// Sub Section Input
		connections.add(new IdeConnection<>(FunctionToNextSubSectionInputModel.class)
				.connectOne((s) -> s.getNextSubSectionInput(), (c) -> c.getPreviousFunction(),
						FunctionEvent.CHANGE_NEXT_SUB_SECTION_INPUT)
				.to(SubSectionInputModel.class)
				.many((t) -> t.getPreviousFunctions(), (c) -> c.getNextSubSectionInput(),
						SubSectionInputEvent.ADD_PREVIOUS_FUNCTION, SubSectionInputEvent.REMOVE_PREVIOUS_FUNCTION)
				.create((s, t, ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().linkFunctionToNextSubSectionInput(s, t)))
				.delete((ctx) -> ctx.getChangeExecutor()
						.execute(ctx.getOperations().removeFunctionToNextSubSectionInput(ctx.getModel()))));
	}

}
