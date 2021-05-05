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
import java.util.Map;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityLoader;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityModel.WoofSecurityEvent;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSecurityModel;

/**
 * Configuration for the {@link WoofSecurityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSecurityItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofSecurityModel, WoofSecurityEvent, WoofSecurityItem> {

	/**
	 * {@link HttpSecurity} name.
	 */
	private String name;

	/**
	 * {@link HttpSecuritySource} {@link Class} name.
	 */
	private String sourceClassName;

	/**
	 * Timeout.
	 */
	private long timeout = 5000;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * Content types.
	 */
	private String contentTypes;

	/**
	 * {@link HttpSecurityType}.
	 */
	private HttpSecurityType<?, ?, ?, ?, ?> type;

	/**
	 * {@link HttpSecurityFlowType} name mapping.
	 */
	private Map<String, String> flowNameMapping;

	/*
	 * ================= AbstractConfigurableItem ========================
	 */

	@Override
	public WoofSecurityModel prototype() {
		return new WoofSecurityModel("Security", null, 0);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofSecurities(), WoofEvent.ADD_WOOF_SECURITY,
				WoofEvent.REMOVE_WOOF_SECURITY);
	}

	@Override
	public Pane visual(WoofSecurityModel model, AdaptedChildVisualFactoryContext<WoofSecurityModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.addNode(heading,
				context.connector(DefaultConnectors.FLOW, WoofHttpContinuationToWoofSecurityModel.class,
						WoofHttpInputToWoofSecurityModel.class, WoofTemplateOutputToWoofSecurityModel.class,
						WoofSecurityOutputToWoofSecurityModel.class, WoofSectionOutputToWoofSecurityModel.class,
						WoofExceptionToWoofSecurityModel.class, WoofProcedureNextToWoofSecurityModel.class,
						WoofProcedureOutputToWoofSecurityModel.class).getNode());
		context.label(heading);
		context.addNode(container, context.childGroup(WoofSecurityOutputItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getHttpSecurityName(), WoofSecurityEvent.CHANGE_HTTP_SECURITY_NAME);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofSecurityModel itemModel) {
		parentModel.addWoofSecurity(itemModel);
	}

	@Override
	public WoofSecurityItem item(WoofSecurityModel model) {
		WoofSecurityItem item = new WoofSecurityItem();
		if (model != null) {
			item.name = model.getHttpSecurityName();
			item.sourceClassName = model.getHttpSecuritySourceClassName();
			item.timeout = model.getTimeout();
			item.properties = this.translateToPropertyList(model.getProperties(), (p) -> p.getName(),
					(p) -> p.getValue());
			item.contentTypes = this.translateToCommaSeparateList(model.getContentTypes(),
					(type) -> type.getContentType());
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 50.0%, gold, goldenrod)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "lightgoldenrodyellow"));
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(new WoofSecurityOutputItem()));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Security");

			// Required
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			builder.clazz("Source").init((item) -> item.sourceClassName).superType(HttpSecuritySource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			builder.text("Timeout").init((item) -> String.valueOf(item.timeout)).validate((ctx) -> {
				Long.parseLong(ctx.getValue().getValue());
			}).setValue((item, value) -> item.timeout = Long.parseLong(value));
			builder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);
			builder.text("Content Types").init((item) -> item.contentTypes)
					.setValue((item, value) -> item.contentTypes = value);

			// Validate by loading type
			builder.validate((ctx) -> {
				WoofSecurityItem item = ctx.getModel();

				// Obtain the HTTP Security Source
				Class<? extends HttpSecuritySource> httpSecuritySourceClass = this.getConfigurableContext()
						.getEnvironmentBridge().loadClass(item.sourceClassName, HttpSecuritySource.class);

				// Obtain the loader
				HttpSecurityLoader loader = HttpSecurityArchitectEmployer.employHttpSecurityLoader(
						this.getConfigurableContext().getEnvironmentBridge().getOfficeFloorCompiler());

				// Load the type
				item.type = loader.loadHttpSecurityType(httpSecuritySourceClass, item.properties);

				// Load the mappings
				item.flowNameMapping = this.translateToNameMappings(item.type.getFlowTypes(),
						(flow) -> flow.getFlowName());
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				String[] contentTypes = this.translateFromCommaSeparatedList(item.contentTypes, (value) -> value)
						.toArray(new String[0]);
				context.execute(context.getOperations().addSecurity(item.name, item.sourceClassName, item.timeout,
						item.properties, contentTypes, item.type));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				String[] contentTypes = this.translateFromCommaSeparatedList(item.contentTypes, (value) -> value)
						.toArray(new String[0]);
				context.execute(
						context.getOperations().refactorSecurity(context.getModel(), item.name, item.sourceClassName,
								item.timeout, item.properties, contentTypes, item.type, item.flowNameMapping));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeSecurity(context.getModel()));
		});
	}

}
