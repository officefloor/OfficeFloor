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
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputModel.SubSectionOutputEvent;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToFunctionModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;

/**
 * Configuration for the {@link SubSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionOutputItem extends
		AbstractItem<SectionModel, SectionChanges, SubSectionModel, SubSectionEvent, SubSectionOutputModel, SubSectionOutputEvent> {

	@Override
	public SubSectionOutputModel prototype() {
		return new SubSectionOutputModel("Output", null, false);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getSubSectionOutputs(), SubSectionEvent.ADD_SUB_SECTION_OUTPUT,
				SubSectionEvent.REMOVE_SUB_SECTION_OUTPUT);
	}

	@Override
	public void loadToParent(SubSectionModel parentModel, SubSectionOutputModel itemModel) {
		parentModel.addSubSectionOutput(itemModel);
	}

	@Override
	public Pane visual(SubSectionOutputModel model, AdaptedChildVisualFactoryContext<SubSectionOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container, context.connector(DefaultConnectors.FLOW, SubSectionOutputToExternalFlowModel.class,
				SubSectionOutputToFunctionModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getSubSectionOutputName(),
				SubSectionOutputEvent.CHANGE_SUB_SECTION_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// External Flow
		connections.add(new IdeConnection<>(SubSectionOutputToExternalFlowModel.class)
				.connectOne((s) -> s.getExternalFlow(), (c) -> c.getSubSectionOutput(),
						SubSectionOutputEvent.CHANGE_EXTERNAL_FLOW)
				.to(ExternalFlowModel.class)
				.many((t) -> t.getSubSectionOutputs(), (c) -> c.getExternalFlow(),
						ExternalFlowEvent.ADD_SUB_SECTION_OUTPUT, ExternalFlowEvent.REMOVE_SUB_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSubSectionOutputToExternalFlow(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeSubSectionOutputToExternalFlow(ctx.getModel()));
				}));

		// Function
		connections.add(new IdeConnection<>(SubSectionOutputToFunctionModel.class)
				.connectOne((s) -> s.getFunction(), (c) -> c.getSubSectionOutput(),
						SubSectionOutputEvent.CHANGE_FUNCTION)
				.to(FunctionModel.class).many((t) -> t.getSubSectionOutputs(), (c) -> c.getFunction(),
						FunctionEvent.ADD_SUB_SECTION_OUTPUT, FunctionEvent.REMOVE_SUB_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSubSectionOutputToFunction(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeSubSectionOutputToFunction(ctx.getModel()));
				}));

		// Sub Section Input
	}

}
