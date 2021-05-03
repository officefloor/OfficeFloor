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
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel.WoofHttpContinuationEvent;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureModel.WoofProcedureEvent;
import net.officefloor.woof.model.woof.WoofProcedureNextModel;
import net.officefloor.woof.model.woof.WoofProcedureNextModel.WoofProcedureNextEvent;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofResourceModel.WoofResourceEvent;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;

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
				context.connector(DefaultConnectors.FLOW, WoofProcedureNextToWoofSectionInputModel.class,
						WoofProcedureNextToWoofTemplateModel.class, WoofProcedureNextToWoofResourceModel.class,
						WoofProcedureNextToWoofSecurityModel.class, WoofProcedureNextToWoofHttpContinuationModel.class,
						WoofProcedureNextToWoofProcedureModel.class).getNode());
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

		// Template
		connections.add(new IdeConnection<>(WoofProcedureNextToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofProcedureNext(),
						WoofProcedureNextEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class)
				.many(t -> t.getWoofProcedureNexts(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_PROCEDURE_NEXT, WoofTemplateEvent.REMOVE_WOOF_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeProcedureNextToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofProcedureNextToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofProcedureNext(),
						WoofProcedureNextEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class)
				.many(t -> t.getWoofProcedureNexts(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_PROCEDURE_NEXT, WoofResourceEvent.REMOVE_WOOF_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeProcedureNextToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofProcedureNextToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofProcedureNext(),
						WoofProcedureNextEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class)
				.many(t -> t.getWoofProcedureNexts(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_PROCEDURE_NEXT, WoofSecurityEvent.REMOVE_WOOF_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeProcedureNextToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections
				.add(new IdeConnection<>(WoofProcedureNextToWoofHttpContinuationModel.class)
						.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofProcedureNext(),
								WoofProcedureNextEvent.CHANGE_WOOF_HTTP_CONTINUATION)
						.to(WoofHttpContinuationModel.class)
						.many(t -> t.getWoofProcedureNexts(), c -> c.getWoofHttpContinuation(),
								WoofHttpContinuationEvent.ADD_WOOF_PROCEDURE_NEXT,
								WoofHttpContinuationEvent.REMOVE_WOOF_PROCEDURE_NEXT)
						.create((s, t, ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().linkProcedureNextToHttpContinuation(s, t));
						}).delete((ctx) -> {
							ctx.getChangeExecutor()
									.execute(ctx.getOperations().removeProcedureNextToHttpContinuation(ctx.getModel()));
						}));

		// Procedure
		connections.add(new IdeConnection<>(WoofProcedureNextToWoofProcedureModel.class)
				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofProcedureNext(),
						WoofProcedureNextEvent.CHANGE_WOOF_PROCEDURE)
				.to(WoofProcedureModel.class)
				.many(t -> t.getWoofProcedureNexts(), c -> c.getWoofProcedure(),
						WoofProcedureEvent.ADD_WOOF_PROCEDURE_NEXT, WoofProcedureEvent.REMOVE_WOOF_PROCEDURE_NEXT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkProcedureNextToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeProcedureNextToProcedure(ctx.getModel()));
				}));
	}

}
