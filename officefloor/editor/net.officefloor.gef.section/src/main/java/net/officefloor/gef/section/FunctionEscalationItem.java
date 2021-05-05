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
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationModel.FunctionEscalationEvent;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;

/**
 * Configuration for the {@link FunctionEscalationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionEscalationItem extends
		AbstractItem<SectionModel, SectionChanges, FunctionModel, FunctionEvent, FunctionEscalationModel, FunctionEscalationEvent> {

	@Override
	public FunctionEscalationModel prototype() {
		return new FunctionEscalationModel("Escalation");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((p) -> p.getFunctionEscalations(), FunctionEvent.ADD_FUNCTION_ESCALATION,
				FunctionEvent.REMOVE_FUNCTION_ESCALATION);
	}

	@Override
	public void loadToParent(FunctionModel parentModel, FunctionEscalationModel itemModel) {
		parentModel.addFunctionEscalation(itemModel);
	}

	@Override
	public Pane visual(FunctionEscalationModel model,
			AdaptedChildVisualFactoryContext<FunctionEscalationModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, FunctionEscalationToFunctionModel.class,
						FunctionEscalationToSubSectionInputModel.class, FunctionEscalationToExternalFlowModel.class)
						.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((m) -> m.getEscalationType(), FunctionEscalationEvent.CHANGE_ESCALATION_TYPE);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Function
		connections.add(new IdeConnection<>(FunctionEscalationToFunctionModel.class)
				.connectOne((s) -> s.getFunction(), (c) -> c.getEscalation(), FunctionEscalationEvent.CHANGE_FUNCTION)
				.to(FunctionModel.class)
				.many((t) -> t.getFunctionEscalationInputs(), (c) -> c.getFunction(),
						FunctionEvent.ADD_FUNCTION_ESCALATION, FunctionEvent.REMOVE_FUNCTION_ESCALATION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkFunctionEscalationToFunction(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeFunctionEscalationToFunction(ctx.getModel()));
				}));

		// Section Input
		connections.add(new IdeConnection<>(FunctionEscalationToSubSectionInputModel.class)
				.connectOne((s) -> s.getSubSectionInput(), (c) -> c.getFunctionEscalation(),
						FunctionEscalationEvent.CHANGE_SUB_SECTION_INPUT)
				.to(SubSectionInputModel.class)
				.many((t) -> t.getFunctionEscalations(), (c) -> c.getSubSectionInput(),
						SubSectionInputEvent.ADD_FUNCTION_ESCALATION, SubSectionInputEvent.REMOVE_FUNCTION_ESCALATION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkFunctionEscalationToSubSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeFunctionEscalationToSubSectionInput(ctx.getModel()));
				}));

		// External Flow
		connections.add(new IdeConnection<>(FunctionEscalationToExternalFlowModel.class)
				.connectOne((s) -> s.getExternalFlow(), (c) -> c.getFunctionEscalation(),
						FunctionEscalationEvent.CHANGE_EXTERNAL_FLOW)
				.to(ExternalFlowModel.class)
				.many((t) -> t.getFunctionEscalations(), (c) -> c.getExternalFlow(),
						ExternalFlowEvent.ADD_FUNCTION_ESCALATION, ExternalFlowEvent.REMOVE_FUNCTION_ESCALATION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkFunctionEscalationToExternalFlow(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeFunctionEscalationToExternalFlow(ctx.getModel()));
				}));
	}

}
