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

import java.io.IOException;
import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.DefaultConnectors;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionModel.WoofExceptionEvent;
import net.officefloor.woof.model.woof.WoofExceptionToWoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationModel.WoofHttpContinuationEvent;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofResourceModel.WoofResourceEvent;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel.WoofSectionInputEvent;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;

/**
 * Configuration for the {@link WoofExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofExceptionItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofExceptionModel, WoofExceptionEvent, WoofExceptionItem> {

	/**
	 * Test configuration.
	 */
	public static void main(String[] args) {
		WoofEditor.launchConfigurer(new WoofExceptionItem(), (model) -> {
			model.setClassName(IOException.class.getName());
		});
	}

	/**
	 * {@link Exception} {@link Class}.
	 */
	private String exceptionClassName;

	/*
	 * ================ AbstractConfigurableItem =====================
	 */

	@Override
	public WoofExceptionModel prototype() {
		return new WoofExceptionModel("Exception");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofExceptions(), WoofEvent.ADD_WOOF_EXCEPTION,
				WoofEvent.REMOVE_WOOF_EXCEPTION);
	}

	@Override
	public Pane visual(WoofExceptionModel model, AdaptedModelVisualFactoryContext<WoofExceptionModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofExceptionToWoofSectionInputModel.class,
						WoofExceptionToWoofTemplateModel.class, WoofExceptionToWoofResourceModel.class,
						WoofExceptionToWoofSecurityModel.class, WoofExceptionToWoofHttpContinuationModel.class)
						.getNode());
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getClassName(), WoofExceptionEvent.CHANGE_CLASS_NAME);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofExceptionModel itemModel) {
		parentModel.addWoofException(itemModel);
	}

	@Override
	protected WoofExceptionItem item(WoofExceptionModel model) {
		WoofExceptionItem item = new WoofExceptionItem();
		if (model != null) {
			item.exceptionClassName = model.getClassName();
		}
		return item;
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Section Input
		connections.add(new IdeConnection<>(WoofExceptionToWoofSectionInputModel.class)
				.connectOne(s -> s.getWoofSectionInput(), c -> c.getWoofException(),
						WoofExceptionEvent.CHANGE_WOOF_SECTION_INPUT)
				.to(WoofSectionInputModel.class)
				.many(t -> t.getWoofExceptions(), c -> c.getWoofSectionInput(),
						WoofSectionInputEvent.ADD_WOOF_EXCEPTION, WoofSectionInputEvent.REMOVE_WOOF_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToSectionInput(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeExceptionToSectionInput(ctx.getModel()));
				}));

		// Template
		connections.add(new IdeConnection<>(WoofExceptionToWoofTemplateModel.class)
				.connectOne(s -> s.getWoofTemplate(), c -> c.getWoofException(),
						WoofExceptionEvent.CHANGE_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class).many(t -> t.getWoofExceptions(), c -> c.getWoofTemplate(),
						WoofTemplateEvent.ADD_WOOF_EXCEPTION, WoofTemplateEvent.REMOVE_WOOF_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeExceptionToTemplate(ctx.getModel()));
				}));

		// Resource
		connections.add(new IdeConnection<>(WoofExceptionToWoofResourceModel.class)
				.connectOne(s -> s.getWoofResource(), c -> c.getWoofException(),
						WoofExceptionEvent.CHANGE_WOOF_RESOURCE)
				.to(WoofResourceModel.class).many(t -> t.getWoofExceptions(), c -> c.getWoofResource(),
						WoofResourceEvent.ADD_WOOF_EXCEPTION, WoofResourceEvent.REMOVE_WOOF_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToResource(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeExceptionToResource(ctx.getModel()));
				}));

		// Security
		connections.add(new IdeConnection<>(WoofExceptionToWoofSecurityModel.class)
				.connectOne(s -> s.getWoofSecurity(), c -> c.getWoofException(),
						WoofExceptionEvent.CHANGE_WOOF_SECURITY)
				.to(WoofSecurityModel.class).many(t -> t.getWoofExceptions(), c -> c.getWoofSecurity(),
						WoofSecurityEvent.ADD_WOOF_EXCEPTION, WoofSecurityEvent.REMOVE_WOOF_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToSecurity(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeExceptionToSecurity(ctx.getModel()));
				}));

		// HTTP Continuation
		connections.add(new IdeConnection<>(WoofExceptionToWoofHttpContinuationModel.class)
				.connectOne(s -> s.getWoofHttpContinuation(), c -> c.getWoofException(),
						WoofExceptionEvent.CHANGE_WOOF_HTTP_CONTINUATION)
				.to(WoofHttpContinuationModel.class)
				.many(t -> t.getWoofExceptions(), c -> c.getWoofHttpContinuation(),
						WoofHttpContinuationEvent.ADD_WOOF_EXCEPTION, WoofHttpContinuationEvent.REMOVE_WOOF_EXCEPTION)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkExceptionToHttpContinuation(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor()
							.execute(ctx.getOperations().removeExceptionToHttpContinuation(ctx.getModel()));
				}));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Exception");
			builder.clazz("Exception").init((item) -> item.exceptionClassName).superType(Throwable.class)
					.validate(ValueValidator.notEmptyString("Must provide exception class"))
					.setValue((item, value) -> item.exceptionClassName = value);

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addException(item.exceptionClassName));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().refactorException(context.getModel(), item.exceptionClassName));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeException(context.getModel()));

		});
	}

}