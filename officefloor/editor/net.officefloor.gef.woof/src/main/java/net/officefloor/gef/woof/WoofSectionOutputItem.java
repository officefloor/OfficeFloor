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
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofResourceModel.WoofResourceEvent;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionModel.WoofSectionEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel.WoofSectionOutputEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;

/**
 * Configuration for the {@link WoofSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofSectionModel, WoofSectionEvent, WoofSectionOutputModel, WoofSectionOutputEvent> {

	@Override
	public WoofSectionOutputModel prototype() {
		return new WoofSectionOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofSectionEvent.ADD_OUTPUT,
				WoofSectionEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofSectionModel parentModel, WoofSectionOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofSectionOutputModel model, AdaptedChildVisualFactoryContext<WoofSectionOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofSectionOutputToWoofSectionInputModel.class,
						WoofSectionOutputToWoofTemplateModel.class, WoofSectionOutputToWoofResourceModel.class,
						WoofSectionOutputToWoofSecurityModel.class, WoofSectionOutputToWoofHttpContinuationModel.class,
						WoofSectionOutputToWoofProcedureModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSectionOutputName(),
				WoofSectionOutputEvent.CHANGE_WOOF_SECTION_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_SECTION_OUTPUT, WoofSectionInputEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeSectionOutputToSectionInput(ctx.getModel()));
				}));

		// Template
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_SECTION_OUTPUT, WoofTemplateEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_SECTION_OUTPUT, WoofResourceEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_SECTION_OUTPUT, WoofSecurityEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections
				.add(new IdeConnection<>(WoofSectionOutputToWoofHttpContinuationModel.class)
						.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofSectionOutput(),
								WoofSectionOutputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
						.to(WoofHttpContinuationModel.class)
						.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofHttpContinuation(),
								WoofHttpContinuationEvent.ADD_WOOF_SECTION_OUTPUT,
								WoofHttpContinuationEvent.REMOVE_WOOF_SECTION_OUTPUT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().linkSectionOutputToHttpContinuation(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().removeSectionOutputToHttpContinuation(ctx.getModel()));
						}));

		// Procedure
		connections.add(new IdeConnection<>(WoofSectionOutputToWoofProcedureModel.class)
				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofSectionOutput(),
						WoofSectionOutputEvent.CHANGE_WOOF_PROCEDURE)
				.to(WoofProcedureModel.class)
				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofProcedure(),
						WoofProcedureEvent.ADD_WOOF_SECTION_OUTPUT, WoofProcedureEvent.REMOVE_WOOF_SECTION_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToProcedure(ctx.getModel()));
				}));
	}

}
