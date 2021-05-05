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

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.gef.configurer.ListBuilder;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateArchitectEmployer;
import net.officefloor.web.template.type.WebTemplateLoader;
import net.officefloor.web.template.type.WebTemplateOutputType;
import net.officefloor.web.template.type.WebTemplateType;
import net.officefloor.woof.model.woof.WoofChangeIssues;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateChangeContext;
import net.officefloor.woof.model.woof.WoofTemplateChangeContextImpl;
import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.model.woof.WoofTemplateLinkModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateModel.WoofTemplateEvent;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateToSuperWoofTemplateModel;
import net.officefloor.woof.template.WoofTemplateExtensionLoaderUtil;

/**
 * Configuration for the {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofTemplateModel, WoofTemplateEvent, WoofTemplateItem> {

	/**
	 * Application path.
	 */
	private String applicationPath;

	/**
	 * Location of the template.
	 */
	private String location;

	/**
	 * Logic {@link Class}.
	 */
	public String logicClass;

	/**
	 * {@link WebTemplateType} for the {@link WebTemplate}.
	 */
	private WebTemplateType type;

	/**
	 * {@link WebTemplateOutputType} name mapping.
	 */
	private Map<String, String> outputNameMapping;

	/**
	 * Redirect values function.
	 */
	private String redirectValuesFunction;

	/**
	 * Indicates if secure.
	 */
	private boolean isSecure;

	/**
	 * {@link LinkSecure} instances.
	 */
	private List<LinkSecure> links = new LinkedList<>();

	/**
	 * Content type.
	 */
	private String contentType;

	/**
	 * {@link Charset} name.
	 */
	private String charset;

	/**
	 * Link separator {@link Character}.
	 */
	private String linkSeparatorCharacter;

	/**
	 * Comma separated list of render HTTP methods.
	 */
	private String renderHttpMethods;

	/**
	 * Obtains the {@link Map} of links secure.
	 * 
	 * @param item {@link WoofTemplateItem}.
	 * @return {@link Map} of link to secure.
	 */
	private Map<String, Boolean> getLinksSecure(WoofTemplateItem item) {
		Map<String, Boolean> links = new HashMap<>();
		for (LinkSecure link : item.links) {
			if ((link.linkName != null) && (link.linkName.trim().length() > 0)) {
				links.put(link.linkName.trim(), link.isSecure);
			}
		}
		return links;
	}

	/**
	 * Obtains the {@link WoofTemplateChangeContext}.
	 * 
	 * @param context {@link ConfigurableContext}.
	 * @return {@link WoofTemplateChangeContext}.
	 * @throws Exception If fails to create {@link WoofTemplateChangeContext}.
	 */
	private WoofTemplateChangeContext getWoofTemplateChangeContext(ConfigurableContext<WoofModel, WoofChanges> context)
			throws Exception {
		// Create the template change
		ClassLoader classLoader = context.getEnvironmentBridge().getClassLoader();
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		SourceContext sourceContext = compiler.createRootSourceContext();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader, null);
		WoofChangeIssues issues = WoofTemplateExtensionLoaderUtil.getWoofChangeIssues();
		WoofTemplateChangeContext changeContext = new WoofTemplateChangeContextImpl(false, sourceContext,
				configurationContext, issues);
		return changeContext;
	}

	/*
	 * ================== AbstractConfigurableItem ====================
	 */

	@Override
	public WoofTemplateModel prototype() {
		return new WoofTemplateModel("Template", null, null, null, null, null, null, false);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofTemplates(), WoofEvent.ADD_WOOF_TEMPLATE,
				WoofEvent.REMOVE_WOOF_TEMPLATE);
	}

	@Override
	public Pane visual(WoofTemplateModel model, AdaptedChildVisualFactoryContext<WoofTemplateModel> context) {
		VBox container = new VBox();
		HBox heading = context.addNode(container, new HBox());
		context.addNode(heading,
				context.connector(DefaultConnectors.FLOW, WoofHttpContinuationToWoofTemplateModel.class,
						WoofHttpInputToWoofTemplateModel.class, WoofTemplateOutputToWoofTemplateModel.class,
						WoofSecurityOutputToWoofTemplateModel.class, WoofSectionOutputToWoofTemplateModel.class,
						WoofExceptionToWoofTemplateModel.class, WoofProcedureNextToWoofTemplateModel.class,
						WoofProcedureOutputToWoofTemplateModel.class).target(WoofTemplateToSuperWoofTemplateModel.class)
						.getNode());
		context.label(heading);
		context.addNode(heading, context.connector(DefaultConnectors.DERIVE)
				.source(WoofTemplateToSuperWoofTemplateModel.class).getNode());
		HBox children = context.addNode(container, new HBox());
		context.addIndent(children);
		context.addNode(children, context.childGroup(WoofTemplateOutputItem.class.getSimpleName(), new VBox()));
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getApplicationPath(), WoofTemplateEvent.CHANGE_APPLICATION_PATH);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofTemplateModel itemModel) {
		parentModel.addWoofTemplate(itemModel);
	}

	@Override
	public WoofTemplateItem item(WoofTemplateModel model) {
		WoofTemplateItem item = new WoofTemplateItem();
		if (model != null) {
			item.applicationPath = model.getApplicationPath();
			item.isSecure = model.getIsTemplateSecure();
			item.location = model.getTemplateLocation();
			item.logicClass = model.getTemplateClassName();
			item.redirectValuesFunction = model.getRedirectValuesFunction();
			item.contentType = model.getTemplateContentType();
			item.charset = model.getTemplateCharset();
			item.linkSeparatorCharacter = model.getLinkSeparatorCharacter();

			// Load render HTTP methods
			item.renderHttpMethods = this.translateToCommaSeparateList(model.getRenderHttpMethods(),
					(method) -> method.getWoofTemplateRenderHttpMethodName());

			// Links
			for (WoofTemplateLinkModel link : model.getLinks()) {
				item.links.add(new LinkSecure(link));
			}
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 50.0%, blue, dodgerblue)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "lightcyan"));
	}

	@Override
	protected void connections(List<IdeConnectionTarget<? extends ConnectionModel, ?, ?>> connections) {

		// Template Inheritance
		connections.add(new IdeConnection<>(WoofTemplateToSuperWoofTemplateModel.class)
				.connectOne(s -> s.getSuperWoofTemplate(), c -> c.getChildWoofTemplate(),
						WoofTemplateEvent.CHANGE_SUPER_WOOF_TEMPLATE)
				.to(WoofTemplateModel.class)
				.many(t -> t.getChildWoofTemplates(), c -> c.getSuperWoofTemplate(),
						WoofTemplateEvent.ADD_CHILD_WOOF_TEMPLATE, WoofTemplateEvent.REMOVE_CHILD_WOOF_TEMPLATE)
				.create((s, t, ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().linkTemplateToSuperTemplate(s, t));
				}).delete((ctx) -> {
					ctx.getChangeExecutor().execute(ctx.getOperations().removeTemplateToSuperTemplate(ctx.getModel()));
				}));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Template");

			// Required values
			builder.text("Path").init((item) -> item.applicationPath)
					.validate(ValueValidator.notEmptyString("Must provide path"))
					.setValue((item, value) -> item.applicationPath = value);
			builder.resource("Location").init((item) -> item.location)
					.validate(ValueValidator.notEmptyString("Must provide location"))
					.setValue((item, value) -> item.location = value);

			// Logic values
			builder.clazz("Logic").init((item) -> item.logicClass).setValue((item, value) -> item.logicClass = value);
			builder.text("Redirect values method").init((item) -> item.redirectValuesFunction).validate((ctx) -> {
				// Ignore if no logic class
				String logicClassName = ctx.getModel().logicClass;
				if ((logicClassName == null) || (logicClassName.trim().length() == 0)) {
					return;
				}

				// Ignore if no redirect method (will be picked up loading type)
				String redirectMethodName = ctx.getValue().getValue();
				if ((redirectMethodName == null) || (redirectMethodName.trim().length() == 0)) {
					return;
				}

				// Ensure method exists on class
				Class<?> logicClass = this.getConfigurableContext().getEnvironmentBridge()
						.loadClass(ctx.getModel().logicClass, Object.class);
				for (Method method : logicClass.getMethods()) {
					if (redirectMethodName.equals(method.getName())) {
						return;
					}
				}
				ctx.setError("No method '" + redirectMethodName + "' on logic class");
			}).setValue((item, value) -> item.redirectValuesFunction = value);

			// Security
			builder.flag("HTTPS").init((item) -> item.isSecure).setValue((item, value) -> item.isSecure = value);
			ListBuilder<WoofTemplateItem, LinkSecure> links = builder.list("Link HTTPS", LinkSecure.class)
					.init((item) -> item.links).setValue((item, value) -> item.links = value)
					.addItem(() -> new LinkSecure()).deleteItem();
			links.text("Link").init((item) -> item.linkName).setValue((item, value) -> item.linkName = value);
			links.flag("HTTPS").init((item) -> item.isSecure).setValue((item, value) -> item.isSecure = value);

			// Optional fields
			builder.text("Render HTTP methods").init((item) -> item.renderHttpMethods)
					.setValue((item, value) -> item.renderHttpMethods = value);
			builder.text("Content-Type").init((item) -> item.contentType)
					.setValue((item, value) -> item.contentType = value);
			builder.text("Charset").init((item) -> item.charset).setValue((item, value) -> item.charset = charset);
			builder.text("Link separator").init((item) -> item.linkSeparatorCharacter).validate((ctx) -> {
				String character = ctx.getValue().getValue();
				if (character == null) {
					character = "";
				}
				if (character.trim().length() > 2) {
					ctx.setError("May only have single character for link separator");
				}
			}).setValue((item, value) -> item.linkSeparatorCharacter = value);

			builder.validate((ctx) -> {
				WoofTemplateItem item = ctx.getModel();

				// Obtain the loader
				WebTemplateLoader loader = WebTemplateArchitectEmployer.employWebTemplateLoader(
						this.getConfigurableContext().getEnvironmentBridge().getOfficeFloorCompiler());

				// Configure the template
				WebTemplate template = loader.addTemplate(item.isSecure, item.applicationPath, item.location);
				template.setLogicClass(item.logicClass);
				template.setRedirectValuesFunction(item.redirectValuesFunction);
				template.setContentType(item.contentType);
				template.setCharset(item.charset);
				String separator = item.linkSeparatorCharacter;
				if ((separator != null) && (separator.length() > 0)) {
					template.setLinkSeparatorCharacter(separator.charAt(0));
				}
				for (String method : this.translateFromCommaSeparatedList(item.renderHttpMethods, (value) -> value)) {
					template.addRenderHttpMethod(method);
				}
				for (LinkSecure secure : item.links) {
					template.setLinkSecure(secure.linkName, secure.isSecure);
				}

				// Load the super templates (if model)
				WoofTemplateModel templateModel = context.getModel();
				if (templateModel != null) {
					context.getOperations().loadSuperTemplates(template, templateModel, loader);
				}

				// Load the type
				item.type = loader.loadWebTemplateType(template);

				// Load the output mappings
				item.outputNameMapping = this.translateToNameMappings(item.type.getWebTemplateOutputTypes(),
						(output) -> output.getWebTemplateOutputName());
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {

				// TODO provide configuration of extensions
				WoofTemplateExtension[] extensions = null;

				// Add template
				Map<String, Boolean> linksSecure = this.getLinksSecure(item);
				String[] renderHttpMethods = this
						.translateFromCommaSeparatedList(item.renderHttpMethods, (value) -> value)
						.toArray(new String[0]);
				WoofTemplateChangeContext changeContext = this
						.getWoofTemplateChangeContext(this.getConfigurableContext());
				context.execute(context.getOperations().addTemplate(item.applicationPath, item.location,
						item.logicClass, item.type, item.redirectValuesFunction, item.contentType, item.charset,
						item.isSecure, item.linkSeparatorCharacter, linksSecure, renderHttpMethods, extensions,
						changeContext));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {

				// TODO provide handling of inherited output mappings
				Set<String> inherited = null; // context.getOperations().getInheritableOutputNames(context.getModel());

				// TODO provide configuration of extensions
				WoofTemplateExtension[] extensions = null;

				// Refactor template
				Map<String, Boolean> linksSecure = this.getLinksSecure(item);
				String[] renderHttpMethods = this
						.translateFromCommaSeparatedList(item.renderHttpMethods, (value) -> value)
						.toArray(new String[0]);
				WoofTemplateChangeContext changeContext = this
						.getWoofTemplateChangeContext(this.getConfigurableContext());
				context.execute(context.getOperations().refactorTemplate(context.getModel(), item.applicationPath,
						item.location, item.logicClass, item.type, item.redirectValuesFunction, inherited,
						item.contentType, item.charset, item.isSecure, item.linkSeparatorCharacter, linksSecure,
						renderHttpMethods, extensions, item.outputNameMapping, changeContext));
			});

		}).delete((context) -> {
			WoofTemplateChangeContext changeContext = this.getWoofTemplateChangeContext(this.getConfigurableContext());
			context.execute(context.getOperations().removeTemplate(context.getModel(), changeContext));
		});
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(new WoofTemplateOutputItem()));
	}

	/**
	 * Link secure.
	 */
	private static class LinkSecure {

		/**
		 * Link name.
		 */
		private String linkName = "";

		/**
		 * Indicates if secure.
		 */
		private boolean isSecure = true;

		/**
		 * New row constructor.
		 */
		private LinkSecure() {
		}

		/**
		 * Load from {@link WoofTemplateLinkModel}.
		 * 
		 * @param link {@link WoofTemplateLinkModel}.
		 */
		private LinkSecure(WoofTemplateLinkModel link) {
			this.linkName = link.getWoofTemplateLinkName();
			this.isSecure = link.getIsLinkSecure();
		}
	}

}
