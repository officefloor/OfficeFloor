/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.section;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowModel.FunctionFlowEvent;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;

/**
 * Configuration for the {@link FunctionFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionFlowItem extends
		AbstractItem<SectionModel, SectionChanges, FunctionModel, FunctionEvent, FunctionFlowModel, FunctionFlowEvent> {

	@Override
	public FunctionFlowModel prototype() {
		return new FunctionFlowModel("Flow", null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((p) -> p.getFunctionFlows(), FunctionEvent.ADD_FUNCTION_FLOW,
				FunctionEvent.REMOVE_FUNCTION_FLOW);
	}

	@Override
	public void loadToParent(FunctionModel parentModel, FunctionFlowModel itemModel) {
		parentModel.addFunctionFlow(itemModel);
	}

	@Override
	public Pane visual(FunctionFlowModel model, AdaptedChildVisualFactoryContext<FunctionFlowModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(
				container, context
						.connector(DefaultConnectors.FLOW, FunctionFlowToFunctionModel.class,
								FunctionFlowToExternalFlowModel.class, FunctionFlowToSubSectionInputModel.class)
						.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((m) -> m.getFlowName(), FunctionFlowEvent.CHANGE_FLOW_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Function
		connections.add(new IdeConnection<>(FunctionFlowToFunctionModel.class)
				.connectOne((s) -> s.getFunction(), (c) -> c.getFunctionFlow(), FunctionFlowEvent.CHANGE_FUNCTION)
				.to(FunctionModel.class).many((s) -> s.getFunctionFlowInputs(), (c) -> c.getFunction(),
						FunctionEvent.ADD_FUNCTION_FLOW_INPUT, FunctionEvent.REMOVE_FUNCTION_FLOW_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkFunctionFlowToFunction(s, t, false));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeFunctionFlowToFunction(ctx.getModel()));
				}));

		// Section Input
		connections.add(new IdeConnection<>(FunctionFlowToSubSectionInputModel.class)
				.connectOne((s) -> s.getSubSectionInput(), (c) -> c.getFunctionFlow(),
						FunctionFlowEvent.CHANGE_SUB_SECTION_INPUT)
				.to(SubSectionInputModel.class)
				.many((t) -> t.getFunctionFlows(), (c) -> c.getSubSectionInput(),
						SubSectionInputEvent.ADD_FUNCTION_FLOW, SubSectionInputEvent.REMOVE_FUNCTION_FLOW)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkFunctionFlowToSubSectionInput(s, t, false));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeFunctionFlowToSubSectionInput(ctx.getModel()));
				}));

		// External Flow
		connections.add(new IdeConnection<>(FunctionFlowToExternalFlowModel.class)
				.connectOne((s) -> s.getExternalFlow(), (c) -> c.getFunctionFlow(),
						FunctionFlowEvent.CHANGE_EXTERNAL_FLOW)
				.to(ExternalFlowModel.class).many((t) -> t.getFunctionFlows(), (c) -> c.getExternalFlow(),
						ExternalFlowEvent.ADD_FUNCTION_FLOW, ExternalFlowEvent.REMOVE_FUNCTION_FLOW)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkFunctionFlowToExternalFlow(s, t, false));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeFunctionFlowToExternalFlow(ctx.getModel()));
				}));
	}

}
