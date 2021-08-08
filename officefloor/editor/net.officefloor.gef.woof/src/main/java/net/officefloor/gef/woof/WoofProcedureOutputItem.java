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

package net.officefloor.gef.woof;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel.WoofHttpContinuationEvent;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureModel.WoofProcedureEvent;
import net.officefloor.woof.model.woof.WoofProcedureOutputModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputModel.WoofProcedureOutputEvent;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofResourceModel.WoofResourceEvent;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;

/**
 * Configuration for the {@link WoofProcedureOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofProcedureOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofProcedureModel, WoofProcedureEvent, WoofProcedureOutputModel, WoofProcedureOutputEvent> {

	@Override
	public WoofProcedureOutputModel prototype() {
		return new WoofProcedureOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofProcedureEvent.ADD_OUTPUT,
				WoofProcedureEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofProcedureModel parentModel, WoofProcedureOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofProcedureOutputModel model,
			AdaptedChildVisualFactoryContext<WoofProcedureOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofProcedureOutputToWoofSectionInputModel.class,
						WoofProcedureOutputToWoofTemplateModel.class, WoofProcedureOutputToWoofResourceModel.class,
						WoofProcedureOutputToWoofSecurityModel.class,
						WoofProcedureOutputToWoofHttpContinuationModel.class,
						WoofProcedureOutputToWoofProcedureModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofProcedureOutputName(),
				WoofProcedureOutputEvent.CHANGE_WOOF_PROCEDURE_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofProcedureOutputToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofProcedureOutput(),
						WoofProcedureOutputEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofProcedureOutputs(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_PROCEDURE_OUTPUT,
						WoofSectionInputEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeProcedureOutputToSectionInput(ctx.getModel()));
				}));

		// Template
		connections.add(new IdeConnection<>(WoofProcedureOutputToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofProcedureOutput(),
						WoofProcedureOutputEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class)
				.many(t -> t.getWoofProcedureOutputs(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_PROCEDURE_OUTPUT, WoofTemplateEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeProcedureOutputToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofProcedureOutputToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofProcedureOutput(),
						WoofProcedureOutputEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class)
				.many(t -> t.getWoofProcedureOutputs(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_PROCEDURE_OUTPUT, WoofResourceEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeProcedureOutputToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofProcedureOutputToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofProcedureOutput(),
						WoofProcedureOutputEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class)
				.many(t -> t.getWoofProcedureOutputs(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_PROCEDURE_OUTPUT, WoofSecurityEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeProcedureOutputToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections
				.add(new IdeConnection<>(WoofProcedureOutputToWoofHttpContinuationModel.class)
						.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofProcedureOutput(),
								WoofProcedureOutputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
						.to(WoofHttpContinuationModel.class)
						.many(t -> t.getWoofProcedureOutputs(), c -> c.getWoofHttpContinuation(),
								WoofHttpContinuationEvent.ADD_WOOF_PROCEDURE_OUTPUT,
								WoofHttpContinuationEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().linkProcedureOutputToHttpContinuation(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor().execute(
									ctx.getOperations().removeProcedureOutputToHttpContinuation(ctx.getModel()));
						}));

		// Procedure
		connections.add(new IdeConnection<>(WoofProcedureOutputToWoofProcedureModel.class)
				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofProcedureOutput(),
						WoofProcedureOutputEvent.CHANGE_WOOF_PROCEDURE)
				.to(WoofProcedureModel.class)
				.many(t -> t.getWoofProcedureOutputs(), c -> c.getWoofProcedure(),
						WoofProcedureEvent.ADD_WOOF_PROCEDURE_OUTPUT, WoofProcedureEvent.REMOVE_WOOF_PROCEDURE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureOutputToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeProcedureOutputToProcedure(ctx.getModel()));
				}));
	}

}
