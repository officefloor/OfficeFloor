/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.woof;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureModel.WoofProcedureEvent;
import net.officefloor.woof.model.woof.WoofProcedureNextModel;
import net.officefloor.woof.model.woof.WoofProcedureNextModel.WoofProcedureNextEvent;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputModel.WoofSectionOutputEvent;

/**
 * Configuration for the {@link WoofProcedureNextModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofProcedureNextItem extends
		AbstractItem<WoofModel, WoofChanges, WoofProcedureModel, WoofProcedureEvent, WoofProcedureNextModel, WoofProcedureNextEvent> {

	@Override
	public WoofProcedureNextModel prototype() {
		return new WoofProcedureNextModel();
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> {
			WoofProcedureNextModel next = parent.getNext();
			return next == null ? Collections.emptyList() : Arrays.asList(parent.getNext());
		}, WoofProcedureEvent.CHANGE_NEXT);
	}

	@Override
	public void loadToParent(WoofProcedureModel parentModel, WoofProcedureNextModel itemModel) {
		parentModel.setNext(itemModel);
	}

	@Override
	public Pane visual(WoofProcedureNextModel model, AdaptedChildVisualFactoryContext<WoofProcedureNextModel> context) {
		VBox container = new VBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofProcedureNextToWoofSectionInputModel.class).getNode());

//		, WoofSectionOutputToWoofSectionInputModel.class,
//		WoofSectionOutputToWoofTemplateModel.class, WoofSectionOutputToWoofResourceModel.class,
//		WoofSectionOutputToWoofSecurityModel.class, WoofSectionOutputToWoofHttpContinuationModel.class,
//		WoofSectionOutputToWoofProcedureModel.class

		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> "");
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofProcedureNextToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofProcedureNext(),
						WoofProcedureNextEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofProcedureNexts(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_PROCEDURE_NEXT, WoofSectionInputEvent.REMOVE_WOOF_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeProcedureNextToSectionInput(ctx.getModel()));
				}));

//		// Template
//		connections.add(new IdeConnection<>(WoofSectionOutputToWoofTemplateModel.class)
//				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofSectionOutput(),
//						WoofSectionOutputEvent.CHANGE_WOOF_TEMPLATE)
//				.to(WoofTemplateModel.class)
//				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofTemplate(),
//						WoofTemplateEvent.ADD_WOOF_SECTION_OUTPUT, WoofTemplateEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToTemplate(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToTemplate(ctx.getModel()));
//				}));
//
//		// Resource
//		connections.add(new IdeConnection<>(WoofSectionOutputToWoofResourceModel.class)
//				.connectOne(s -> s.getWoofResource(), c -> c.getWoofSectionOutput(),
//						WoofSectionOutputEvent.CHANGE_WOOF_RESOURCE)
//				.to(WoofResourceModel.class)
//				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofResource(),
//						WoofResourceEvent.ADD_WOOF_SECTION_OUTPUT, WoofResourceEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToResource(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToResource(ctx.getModel()));
//				}));
//
//		// Security
//		connections.add(new IdeConnection<>(WoofSectionOutputToWoofSecurityModel.class)
//				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofSectionOutput(),
//						WoofSectionOutputEvent.CHANGE_WOOF_SECURITY)
//				.to(WoofSecurityModel.class)
//				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofSecurity(),
//						WoofSecurityEvent.ADD_WOOF_SECTION_OUTPUT, WoofSecurityEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToSecurity(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToSecurity(ctx.getModel()));
//				}));
//
//		// HTTP Continuation
//		connections
//				.add(new IdeConnection<>(WoofSectionOutputToWoofHttpContinuationModel.class)
//						.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofSectionOutput(),
//								WoofSectionOutputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
//						.to(WoofHttpContinuationModel.class)
//						.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofHttpContinuation(),
//								WoofHttpContinuationEvent.ADD_WOOF_SECTION_OUTPUT,
//								WoofHttpContinuationEvent.REMOVE_WOOF_SECTION_OUTPUT)
//						.create((s, t, ctx) -> {
//							ctx.getChangeExecutor()
//									.execute(ctx.getOperations().linkSectionOutputToHttpContinuation(s, t));
//						}).delete((ctx) -> {
//							ctx.getChangeExecutor()
//									.execute(ctx.getOperations().removeSectionOutputToHttpContinuation(ctx.getModel()));
//						}));
//
//		// Procedure
//		connections.add(new IdeConnection<>(WoofSectionOutputToWoofProcedureModel.class)
//				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofSectionOutput(),
//						WoofSectionOutputEvent.CHANGE_WOOF_PROCEDURE)
//				.to(WoofProcedureModel.class)
//				.many(t -> t.getWoofSectionOutputs(), c -> c.getWoofProcedure(),
//						WoofProcedureEvent.ADD_WOOF_SECTION_OUTPUT, WoofProcedureEvent.REMOVE_WOOF_SECTION_OUTPUT)
//				.create((s, t, ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().linkSectionOutputToProcedure(s, t));
//				}).delete((ctx) -> {
//					ctx.getChangeExecutor().execute(ctx.getOperations().removeSectionOutputToProcedure(ctx.getModel()));
//				}));
	}

}