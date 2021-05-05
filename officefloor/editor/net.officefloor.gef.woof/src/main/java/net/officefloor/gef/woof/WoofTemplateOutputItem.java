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
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel.WoofTemplateOutputEvent;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofTemplateModel;

/**
 * Configuration for the {@link WoofTemplateOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofTemplateModel, WoofTemplateEvent, WoofTemplateOutputModel, WoofTemplateOutputEvent> {

	/*
	 * ============= AbstractItem ==================
	 */

	@Override
	public WoofTemplateOutputModel prototype() {
		return new WoofTemplateOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofTemplateEvent.ADD_OUTPUT,
				WoofTemplateEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofTemplateModel parentModel, WoofTemplateOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofTemplateOutputModel model,
			AdaptedChildVisualFactoryContext<WoofTemplateOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container, context.connector(DefaultConnectors.FLOW)
				.target(WoofTemplateOutputToWoofSectionInputModel.class, WoofTemplateOutputToWoofTemplateModel.class,
						WoofTemplateOutputToWoofResourceModel.class, WoofTemplateOutputToWoofSecurityModel.class,
						WoofTemplateOutputToWoofHttpContinuationModel.class,
						WoofTemplateOutputToWoofProcedureModel.class)
				.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofTemplateOutputName(),
				WoofTemplateOutputEvent.CHANGE_WOOF_TEMPLATE_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofTemplateOutputToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofTemplateOutput(),
						WoofTemplateOutputEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_TEMPLATE_OUTPUT,
						WoofSectionInputEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeTemplateOutputToSectionInput(ctx.getModel()));
				}));

		// Template
		connections.add(new IdeConnection<>(WoofTemplateOutputToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofTemplateOutput(),
						WoofTemplateOutputEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class)
				.many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_TEMPLATE_OUTPUT, WoofTemplateEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateOutputToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofTemplateOutputToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofTemplateOutput(),
						WoofTemplateOutputEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class)
				.many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_TEMPLATE_OUTPUT, WoofResourceEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateOutputToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofTemplateOutputToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofTemplateOutput(),
						WoofTemplateOutputEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class)
				.many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_TEMPLATE_OUTPUT, WoofSecurityEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateOutputToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections.add(new IdeConnection<>(WoofTemplateOutputToWoofHttpContinuationModel.class)
				.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofTemplateOutput(),
						WoofTemplateOutputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
				.to(WoofHttpContinuationModel.class).many(t -> t.getWoofTemplateOutputs(),
						c -> c.getWoofHttpContinuation(), WoofHttpContinuationEvent.ADD_WOOF_TEMPLATE_OUTPUT,
						WoofHttpContinuationEvent.REMOVE_WOOF_TEMPLATE_OUTPUT));
		// TODO implement Template Output to HTTP Continuation

		// Procedure
		connections.add(new IdeConnection<>(WoofTemplateOutputToWoofProcedureModel.class)
				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofTemplateOutput(),
						WoofTemplateOutputEvent.CHANGE_WOOF_PROCEDURE)
				.to(WoofProcedureModel.class)
				.many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofProcedure(),
						WoofProcedureEvent.ADD_WOOF_TEMPLATE_OUTPUT, WoofProcedureEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeTemplateOutputToProcedure(ctx.getModel()));
				}));
	}

}
