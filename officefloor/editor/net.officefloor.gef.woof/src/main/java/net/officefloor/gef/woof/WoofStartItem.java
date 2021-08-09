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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofProcedureModel;
import net.officefloor.woof.model.woof.WoofProcedureModel.WoofProcedureEvent;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartModel.WoofStartEvent;
import net.officefloor.woof.model.woof.WoofStartToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;

/**
 * Configuration for the {@link WoofStartModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofStartItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofStartModel, WoofStartEvent, WoofStartItem> {

	/*
	 * ======================= AbstractConfigurableItem =====================
	 */

	@Override
	public WoofStartModel prototype() {
		return new WoofStartModel();
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofStarts(), WoofEvent.ADD_WOOF_START,
				WoofEvent.REMOVE_WOOF_START);
	}

	@Override
	public Node visual(WoofStartModel model, AdaptedChildVisualFactoryContext<WoofStartModel> context) {
		HBox container = new HBox();
		context.addNode(container, new Label("Start"));
		context.addNode(container, context.connector(DefaultConnectors.FLOW, WoofStartToWoofSectionInputModel.class,
				WoofStartToWoofProcedureModel.class).getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return null;
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofStartModel itemModel) {
		parentModel.addWoofStart(itemModel);
	}

	@Override
	public WoofStartItem item(WoofStartModel model) {
		return new WoofStartItem();
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofStartToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofStart(),
						WoofStartEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class).many(t -> t.getWoofStarts(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_START, WoofSectionInputEvent.REMOVE_WOOF_START)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkStartToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeStartToSectionInput(ctx.getModel()));
				}));

		// Procedure
		connections.add(new IdeConnection<>(WoofStartToWoofProcedureModel.class)
				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofStart(), WoofStartEvent.CHANGE_WOOF_PROCEDURE)
				.to(WoofProcedureModel.class).many(t -> t.getWoofStarts(), c -> c.getWoofProcedure(),
						WoofProcedureEvent.ADD_WOOF_START, WoofProcedureEvent.REMOVE_WOOF_START)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkStartToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeStartToProcedure(ctx.getModel()));
				}));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().add((context) -> {
			context.execute(context.getOperations().addStart());
		}).delete((context) -> {
			context.execute(context.getOperations().removeStart(context.getModel()));
		});
	}

}
