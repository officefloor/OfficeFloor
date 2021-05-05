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
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AggregateChange;
import net.officefloor.woof.model.woof.DocumentationModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel.WoofHttpContinuationEvent;
import net.officefloor.woof.model.woof.WoofHttpInputModel;
import net.officefloor.woof.model.woof.WoofHttpInputModel.WoofHttpInputEvent;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofProcedureModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
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

/**
 * Configuration for the {@link WoofHttpInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofHttpInputItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofHttpInputModel, WoofHttpInputEvent, WoofHttpInputItem> {

	/**
	 * HTTP method.
	 */
	private String httpMethod = "POST";

	/**
	 * Application path.
	 */
	private String applicationPath;

	/**
	 * Indicates if HTTPS.
	 */
	private boolean isHttps = false;

	/**
	 * Description.
	 */
	private String description;

	/*
	 * ================= AbstractConfigurableItem ==================
	 */

	@Override
	public WoofHttpInputModel prototype() {
		return new WoofHttpInputModel(false, "HTTP", "Input");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofHttpInputs(), WoofEvent.ADD_WOOF_HTTP_INPUT,
				WoofEvent.REMOVE_WOOF_HTTP_INPUT);
	}

	@Override
	public Pane visual(WoofHttpInputModel model, AdaptedChildVisualFactoryContext<WoofHttpInputModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW)
						.target(WoofHttpInputToWoofSectionInputModel.class, WoofHttpInputToWoofTemplateModel.class,
								WoofHttpInputToWoofResourceModel.class, WoofHttpInputToWoofSecurityModel.class,
								WoofHttpInputToWoofHttpContinuationModel.class, WoofHttpInputToWoofProcedureModel.class)
						.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getHttpMethod() + " " + model.getApplicationPath(),
				WoofHttpInputEvent.CHANGE_HTTP_METHOD, WoofHttpInputEvent.CHANGE_APPLICATION_PATH);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofHttpInputModel itemModel) {
		parentModel.addWoofHttpInput(itemModel);
	}

	@Override
	public WoofHttpInputItem item(WoofHttpInputModel model) {
		WoofHttpInputItem item = new WoofHttpInputItem();
		if (model != null) {
			item.httpMethod = model.getHttpMethod();
			item.applicationPath = model.getApplicationPath();
			item.isHttps = model.getIsSecure();
			DocumentationModel documentation = model.getDocumentation();
			if (documentation != null) {
				item.description = documentation.getDescription();
			}
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(
				new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 50.0%, dodgerblue, lightskyblue)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "blue"));
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofHttpInputToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofHttpInput(),
						WoofHttpInputEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofHttpInputs(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_HTTP_INPUT, WoofSectionInputEvent.REMOVE_WOOF_HTTP_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkHttpInputToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeHttpInputToSectionInput(ctx.getModel()));
				}));

		// Template
		connections.add(new IdeConnection<>(WoofHttpInputToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofHttpInput(),
						WoofHttpInputEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class).many(t -> t.getWoofHttpInputs(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_HTTP_INPUT, WoofTemplateEvent.REMOVE_WOOF_HTTP_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkHttpInputToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeHttpInputToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofHttpInputToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofHttpInput(),
						WoofHttpInputEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class).many(t -> t.getWoofHttpInputs(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_HTTP_INPUT, WoofResourceEvent.REMOVE_WOOF_HTTP_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkHttpInputToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeHttpInputToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofHttpInputToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofHttpInput(),
						WoofHttpInputEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class).many(t -> t.getWoofHttpInputs(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_HTTP_INPUT, WoofSecurityEvent.REMOVE_WOOF_HTTP_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkHttpInputToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeHttpInputToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections.add(new IdeConnection<>(WoofHttpInputToWoofHttpContinuationModel.class)
				.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofHttpInput(),
						WoofHttpInputEvent.CHANGE_WOOF_HTTP_CONTINUATION)
				.to(WoofHttpContinuationModel.class)
				.many(t -> t.getWoofHttpInputs(), c -> c.getWoofHttpContinuation(),
						WoofHttpContinuationEvent.ADD_WOOF_HTTP_INPUT, WoofHttpContinuationEvent.REMOVE_WOOF_HTTP_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkHttpInputToHttpContinuation(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeHttpInputToHttpContinuation(ctx.getModel()));
				}));

		// Procedure
		connections.add(new IdeConnection<>(WoofHttpInputToWoofProcedureModel.class)
				.connectOne(s -> s.getWoofProcedure(), c -> c.getWoofHttpInput(),
						WoofHttpInputEvent.CHANGE_WOOF_PROCEDURE)
				.to(WoofProcedureModel.class).many(t -> t.getWoofHttpInputs(), c -> c.getWoofProcedure(),
						WoofProcedureEvent.ADD_WOOF_HTTP_INPUT, WoofProcedureEvent.REMOVE_WOOF_HTTP_INPUT)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkHttpInputToProcedure(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeHttpInputToProcedure(ctx.getModel()));
				}));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("HTTP Input");
			builder.text("HTTP Method").init((item) -> item.httpMethod)
					.validate(ValueValidator.notEmptyString("Must specify HTTP method"))
					.setValue((item, value) -> item.httpMethod = value);
			builder.text("Path").init((item) -> item.applicationPath)
					.validate(ValueValidator.notEmptyString("Must specify application path"))
					.setValue((item, value) -> item.applicationPath = value);
			builder.flag("https").init((item) -> item.isHttps).setValue((item, value) -> item.isHttps = value);
			builder.text("Description").multiline(true).init((item) -> item.description)
					.setValue((item, value) -> item.description = value);

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				Change<WoofHttpInputModel> change = context.getOperations().addHttpInput(item.applicationPath,
						item.httpMethod, item.isHttps);
				context.execute(AggregateChange.aggregate(change,
						context.getOperations().addDocumentation(change.getTarget(), item.description)));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(AggregateChange.aggregate(
						context.getOperations().refactorHttpInput(context.getModel(), item.applicationPath,
								item.httpMethod, item.isHttps),
						context.getOperations().addDocumentation(context.getModel(), item.description)));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeHttpInput(context.getModel()));
		});
	}

}
