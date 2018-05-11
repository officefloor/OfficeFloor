/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.woof;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel.WoofSecurityOutputEvent;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;

/**
 * Configuration for the {@link WoofSecurityOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSecurityOutputItem extends
		AbstractItem<WoofModel, WoofChanges, WoofSecurityModel, WoofSecurityEvent, WoofSecurityOutputModel, WoofSecurityOutputEvent> {

	/*
	 * ============= AbstractItem ==================
	 */

	@Override
	public WoofSecurityOutputModel prototype() {
		return new WoofSecurityOutputModel("Output", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getOutputs(), WoofSecurityEvent.ADD_OUTPUT,
				WoofSecurityEvent.REMOVE_OUTPUT);
	}

	@Override
	public void loadToParent(WoofSecurityModel parentModel, WoofSecurityOutputModel itemModel) {
		parentModel.addOutput(itemModel);
	}

	@Override
	public Pane visual(WoofSecurityOutputModel model,
			AdaptedModelVisualFactoryContext<WoofSecurityOutputModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSecurityOutputName(),
				WoofSecurityOutputEvent.CHANGE_WOOF_SECURITY_OUTPUT_NAME);
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofSecurityOutputToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofSecurityOutput(),
						WoofSecurityOutputEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofSecurityOutputs(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_SECURITY_OUTPUT,
						WoofSectionInputEvent.REMOVE_WOOF_SECURITY_OUTPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkSecurityOutputToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeSecurityOuputToSectionInput(ctx.getModel()));
				}));

		// // Template
		// connections.add(new
		// IdeConnection<>(WoofTemplateOutputToWoofTemplateModel.class)
		// .connectOne(s -> s.getWoofTemplate(), c -> c.getWoofTemplateOutput(),
		// WoofTemplateOutputEvent.CHANGE_WOOF_TEMPLATE)
		// .to(WoofTemplateModel.class)
		// .many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofTemplate(),
		// WoofTemplateEvent.ADD_WOOF_TEMPLATE_OUTPUT,
		// WoofTemplateEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
		// .create((s, t, ctx) -> {
		// ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToTemplate(s,
		// t));
		// }).delete((ctx) -> {
		// ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateOuputToTemplate(ctx.getModel()));
		// }));
		//
		// // Resource
		// connections.add(new
		// IdeConnection<>(WoofTemplateOutputToWoofResourceModel.class)
		// .connectOne(s -> s.getWoofResource(), c -> c.getWoofTemplateOutput(),
		// WoofTemplateOutputEvent.CHANGE_WOOF_RESOURCE)
		// .to(WoofResourceModel.class).many(t -> t.getWoofTemplateOutputs(), c ->
		// c.getWoofResource(),
		// WoofResourceEvent.ADD_WOOF_HTTP_INPUT,
		// WoofResourceEvent.REMOVE_WOOF_HTTP_INPUT)
		// .create((s, t, ctx) -> {
		// ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToResource(s,
		// t));
		// }).delete((ctx) -> {
		// ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateOuputToResource(ctx.getModel()));
		// }));
		//
		// // Security
		// connections.add(new
		// IdeConnection<>(WoofTemplateOutputToWoofSecurityModel.class)
		// .connectOne(s -> s.getWoofSecurity(), c -> c.getWoofTemplateOutput(),
		// WoofTemplateOutputEvent.CHANGE_WOOF_SECURITY)
		// .to(WoofSecurityModel.class)
		// .many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofSecurity(),
		// WoofSecurityEvent.ADD_WOOF_TEMPLATE_OUTPUT,
		// WoofSecurityEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
		// .create((s, t, ctx) -> {
		// ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToSecurity(s,
		// t));
		// }).delete((ctx) -> {
		// ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateOuputToSecurity(ctx.getModel()));
		// }));
		//
		// // HTTP Continuation
		// connections
		// .add(new IdeConnection<>(WoofTemplateOutputToWoofHttpContinuationModel.class)
		// .connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofTemplateOutput(),
		// WoofTemplateOutputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
		// .to(WoofHttpContinuationModel.class)
		// .many(t -> t.getWoofTemplateOutputs(), c -> c.getWoofHttpContinuation(),
		// WoofHttpContinuationEvent.ADD_WOOF_TEMPLATE_OUTPUT,
		// WoofHttpContinuationEvent.REMOVE_WOOF_TEMPLATE_OUTPUT)
		// .create((s, t, ctx) -> {
		//
		// // TODO REMOVE
		// throw new UnsupportedOperationException(
		// "TODO implement connect TemplateOutput to HTTP Continuation");
		//
		// //
		// ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateOutputToHttpContinuation(s,
		// // t));
		// }).delete((ctx) -> {
		//
		// // TODO REMOVE
		// throw new UnsupportedOperationException(
		// "TODO implement remove TemplateOutput to HTTP Continuation");
		//
		// // ctx.getChangeExecutor().execute(
		// //
		// ctx.getOperations().removeTemplateOutputToHttpContinuation(ctx.getModel()));
		// }));
	}

}