/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.wizard.template;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.dialog.input.WoofFileInput;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.WoofExtensionUtil;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtension;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.access.HttpSecuritySourceInstanceContext;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.model.woof.WoofTemplateInheritance;
import net.officefloor.model.woof.WoofTemplateLinkModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.plugin.woof.WoofContextConfigurable;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Wizard page providing the details of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWizardPage extends WizardPage implements
		CompilerIssues, SectionSourceExtensionContext,
		HttpTemplateExtensionSourceInstanceContext {

	/**
	 * {@link Property} specifying the super {@link WoofTemplateModel} name.
	 */
	private static final String SUPER_TEMPLATE_NAME = "super.template.name";

	/**
	 * {@link Property} indicating whether may continue rendering.
	 */
	private static final String PROPERTY_IS_CONTINUE_RENDERING = "is.continue.rendering";

	/**
	 * Creates the mapping of {@link WoofTemplateExtensionSource} class name to
	 * its {@link HttpTemplateExtensionInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link HttpSecuritySourceInstanceContext}.
	 * @return Mapping of {@link WoofTemplateExtensionSource} class name to its
	 *         {@link HttpTemplateExtensionInstance}.
	 */
	public static Map<String, HttpTemplateExtensionSourceInstance> createHttpTemplateExtensionSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			HttpTemplateExtensionSourceInstanceContext context) {

		// Obtain extension source instances (by class name for unique set)
		Map<String, HttpTemplateExtensionSourceInstance> extensionSourceInstances = new HashMap<String, HttpTemplateExtensionSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					WoofTemplateExtensionSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				extensionSourceInstances.put(className,
						new HttpTemplateExtensionSourceInstance(className,
								null, context, project));
			}
		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (WoofTemplateExtensionSourceExtension<?> woofTemplateExtensionSourceExtension : WoofExtensionUtil
				.createWoofTemplateExtensionSourceExtensionList()) {
			try {
				Class<?> woofTemplateExtensionSourceClass = woofTemplateExtensionSourceExtension
						.getWoofTemplateExtensionSourceClass();
				String woofTemplateExtensionSourceClassName = woofTemplateExtensionSourceClass
						.getName();
				extensionSourceInstances.put(
						woofTemplateExtensionSourceClassName,
						new HttpTemplateExtensionSourceInstance(
								woofTemplateExtensionSourceClassName,
								woofTemplateExtensionSourceExtension, context,
								project));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for "
						+ woofTemplateExtensionSourceExtension.getClass()
								.getName(), ex);
			}
		}

		// Return HTTP template extension source instances by class name
		return extensionSourceInstances;
	}

	/**
	 * Obtains the text value.
	 * 
	 * @param value
	 *            Value that may be <code>null</code>.
	 * @return Text value with blank string for <code>null</code> value.
	 */
	private static String getTextValue(String value) {
		return (value == null ? "" : value);
	}

	/**
	 * {@link HttpTemplateInstance} to base decisions. May be <code>null</code>
	 * if creating new {@link HttpTemplateInstance}.
	 */
	private final HttpTemplateInstance templateInstance;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link SectionLoader}.
	 */
	private final SectionLoader sectionLoader;

	/**
	 * {@link ResourceSource} instances.
	 */
	private final ResourceSource[] resourceSources;

	/**
	 * Initial URI path for the {@link HttpTemplate}.
	 */
	private final String initialUriPath;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties;

	/**
	 * URI path for the {@link HttpTemplate}.
	 */
	private final Property uriPath;

	/**
	 * Path to the {@link HttpTemplate}.
	 */
	private String templatePath;

	/**
	 * Logic class name.
	 */
	private final Property logicClassName;

	/**
	 * Super {@link WoofTemplateModel} name.
	 */
	private final Property superTemplateName;

	/**
	 * Inherited template paths.
	 */
	private final Property inheritedTemplatePaths;

	/**
	 * Indicates if the template is secure.
	 */
	private final Property isTemplateSecure;

	/**
	 * Listing of {@link WoofTemplateLinkModel} instances.
	 */
	private WoofTemplateLinkModel[] linksSecure = new WoofTemplateLinkModel[0];

	/**
	 * HTTP methods that trigger a redirect on rendering the
	 * {@link HttpTemplate}.
	 */
	private final Property renderRedirectHttpMethods;

	/**
	 * Indicates whether allow continue rendering.
	 */
	private final Property isContinueRendering;

	/**
	 * {@link WoofTemplateInheritance} for the super {@link WoofTemplateModel}.
	 */
	private WoofTemplateInheritance superTemplateInheritance;

	/**
	 * {@link WoofTemplateInheritance} instances by their
	 * {@link WoofTemplateModel} name.
	 */
	private final Map<String, WoofTemplateInheritance> templateInheritances;

	/**
	 * {@link SectionType} for the {@link HttpTemplateInstance}.
	 */
	private SectionType sectionType = null;

	/**
	 * Available {@link HttpTemplateExtensionSourceInstance} listing.
	 */
	private HttpTemplateExtensionSourceInstance[] availableHttpTemplateExtensionSourceInstances;

	/**
	 * {@link HttpTemplateExtensionStruct} instances.
	 */
	private final List<HttpTemplateExtensionStruct> templateExtensions = new LinkedList<HttpTemplateExtensionStruct>();

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param templateInstance
	 *            {@link HttpTemplateInstance}.
	 * @param templateInheritances
	 *            {@link WoofTemplateInheritance} instances by their
	 *            {@link WoofTemplateModel} name.
	 */
	protected HttpTemplateWizardPage(final IProject project,
			final AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			HttpTemplateInstance templateInstance,
			Map<String, WoofTemplateInheritance> templateInheritances) {
		super("HTTP Template");
		this.project = project;
		this.templateInstance = templateInstance;

		// Obtain the class loader for the project
		this.classLoader = ProjectClassLoader.create(project);

		// Configure the OfficeFloor compiler to obtain the section loader
		this.compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(this.classLoader);
		this.compiler.setCompilerIssues(this);

		// Provide configuration of WoOF context
		final List<ResourceSource> resourceSources = new LinkedList<ResourceSource>();
		WoofContextConfigurable configurable = new WoofContextConfigurable() {

			@Override
			public void addProperty(String name, String value) {
				HttpTemplateWizardPage.this.compiler.addProperty(name, value);
			}

			@Override
			public void setWebAppDirectory(File webAppDir) {
				// Do nothing
			}

			@Override
			public void addResources(ResourceSource resourceSource) {
				HttpTemplateWizardPage.this.compiler
						.addResources(resourceSource);
				resourceSources.add(resourceSource);
			}
		};
		this.resourceSources = resourceSources
				.toArray(new ResourceSource[resourceSources.size()]);

		// Load access to web resources
		File projectDir = project.getLocation().toFile();
		WoofOfficeFloorSource.loadWebResourcesFromMavenProject(configurable,
				projectDir);

		// Obtain the section loader
		this.sectionLoader = this.compiler.getSectionLoader();

		// Obtain template inheritances copy (as may remove refactor instance)
		this.templateInheritances = new HashMap<String, WoofTemplateInheritance>();
		this.templateInheritances.putAll(templateInheritances);

		// Create the property list (and load existing properties)
		this.properties = this.compiler.createPropertyList();
		String initialUriPath = null;
		String initialLogicClassName = null;
		String initialSuperTemplateName = null;
		String initialInheritedTemplatePaths = null;
		String initialIsTemplateSecure = null;
		String initialRenderRedirectHttpMethods = null;
		String initialIsContinueRendering = null;
		if (this.templateInstance != null) {
			// Provide initial values from existing configuration
			initialUriPath = this.templateInstance.getUri();
			initialLogicClassName = this.templateInstance.getLogicClassName();
			initialIsTemplateSecure = String.valueOf(this.templateInstance
					.isTemplateSecure());
			initialIsContinueRendering = String.valueOf(this.templateInstance
					.isContinueRendering());

			// Remove the template from inheriting itself
			String woofTemplateName = this.templateInstance
					.getWoofTemplateName();
			this.templateInheritances.remove(woofTemplateName);

			// Provide the initial super template (if inheriting)
			WoofTemplateModel superTemplate = this.templateInstance
					.getSuperTemplate();
			if (superTemplate != null) {
				// Specify the initial super template
				initialSuperTemplateName = superTemplate.getWoofTemplateName();
				this.superTemplateInheritance = this.templateInheritances
						.get(initialSuperTemplateName);

				// Specify initial inheriting template paths
				if (this.superTemplateInheritance != null) {
					initialInheritedTemplatePaths = this.superTemplateInheritance
							.getInheritedTemplatePathsPropertyValue();
				}
			}

			// Create the render redirect HTTP methods value
			String[] initialRenderRedirectHttpMethodsList = this.templateInstance
					.getRenderRedirectHttpMethods();
			if (initialRenderRedirectHttpMethodsList != null) {
				StringBuilder httpMethods = new StringBuilder();
				boolean isFirst = true;
				for (String httpMethod : initialRenderRedirectHttpMethodsList) {
					if (!isFirst) {
						httpMethods.append(", ");
					}
					isFirst = false;
					httpMethods.append(httpMethod);
				}
				initialRenderRedirectHttpMethods = httpMethods.toString();
			}

			// Configure the links secure (ensure consistent order)
			Map<String, Boolean> configuredLinks = this.templateInstance
					.getLinksSecure();
			List<String> linkNames = new ArrayList<String>(
					configuredLinks.keySet());
			Collections.sort(linkNames, String.CASE_INSENSITIVE_ORDER);
			this.linksSecure = new WoofTemplateLinkModel[linkNames.size()];
			int linkIndex = 0;
			for (String linkName : linkNames) {
				this.linksSecure[linkIndex++] = new WoofTemplateLinkModel(
						linkName, configuredLinks.get(linkName).booleanValue());
			}
		}

		// Specify the initial URI path
		this.initialUriPath = initialUriPath;

		// Add the properties
		this.uriPath = this
				.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI,
						initialUriPath);
		this.logicClassName = this.addProperty(
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				initialLogicClassName);
		this.superTemplateName = this.addProperty(SUPER_TEMPLATE_NAME,
				initialSuperTemplateName);
		this.inheritedTemplatePaths = this.addProperty(
				HttpTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES,
				initialInheritedTemplatePaths);
		this.isTemplateSecure = this.addProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				initialIsTemplateSecure);
		this.renderRedirectHttpMethods = this
				.addProperty(
						HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
						initialRenderRedirectHttpMethods);
		this.isContinueRendering = this.addProperty(
				PROPERTY_IS_CONTINUE_RENDERING, initialIsContinueRendering);

		// Obtain the map of HTTP template extension source instances
		Map<String, HttpTemplateExtensionSourceInstance> httpTemplateExtensionSourceInstanceMap = createHttpTemplateExtensionSourceInstanceMap(
				this.classLoader, project, this);

		// Obtain the HTTP template extension source instances (in order)
		this.availableHttpTemplateExtensionSourceInstances = httpTemplateExtensionSourceInstanceMap
				.values().toArray(new HttpTemplateExtensionSourceInstance[0]);
		Arrays.sort(this.availableHttpTemplateExtensionSourceInstances,
				new Comparator<HttpTemplateExtensionSourceInstance>() {
					@Override
					public int compare(HttpTemplateExtensionSourceInstance a,
							HttpTemplateExtensionSourceInstance b) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								a.getWoofTemplateExtensionSourceClassName(),
								b.getWoofTemplateExtensionSourceClassName());
					}
				});

		// Specify the title
		this.setTitle("Add Template");
	}

	/**
	 * Adds a {@link Property}.
	 * 
	 * @param propertyName
	 *            Name of the {@link Property}.
	 * @param propertyValue
	 *            Value for the {@link Property}.
	 * @return Added {@link Property}.
	 */
	private Property addProperty(String propertyName, String propertyValue) {
		Property property = this.properties.addProperty(propertyName);
		property.setValue(propertyValue);
		return property;
	}

	/**
	 * Obtains the URI path.
	 * 
	 * @return URI path.
	 */
	public String getUriPath() {
		return this.uriPath.getValue();
	}

	/**
	 * Obtains the template path.
	 * 
	 * @return Template path.
	 */
	public String getTemplatePath() {
		return this.templatePath;
	}

	/**
	 * Obtains the logic class name.
	 * 
	 * @return Logic class name.
	 */
	public String getLogicClassName() {
		String className = this.logicClassName.getValue();
		return EclipseUtil.isBlank(className) ? null : className;
	}

	/**
	 * Obtains the {@link SectionType} for the {@link HttpTemplateInstance}.
	 * 
	 * @return {@link SectionType} for the {@link HttpTemplateInstance}.
	 */
	public SectionType getSectionType() {
		return this.sectionType;
	}

	/**
	 * Obtains the {@link WoofTemplateInheritance} for the super
	 * {@link WoofTemplateModel}.
	 * 
	 * @return {@link WoofTemplateInheritance} for the super
	 *         {@link WoofTemplateModel}.
	 */
	public WoofTemplateInheritance getSuperTemplateInheritance() {
		return this.superTemplateInheritance;
	}

	/**
	 * Indicates if the {@link HttpTemplate} requires a secure
	 * {@link ServerHttpConnection}.
	 * 
	 * @return <code>true</code> should the {@link HttpTemplate} requires a
	 *         secure {@link ServerHttpConnection}.
	 */
	public boolean isTemplateSecure() {
		return Boolean.parseBoolean(this.isTemplateSecure.getValue());
	}

	/**
	 * Obtains the {@link HttpTemplate} link override secure configuration.
	 * 
	 * @return {@link HttpTemplate} link override secure configuration.
	 */
	public Map<String, Boolean> getLinksSecure() {
		Map<String, Boolean> results = new HashMap<String, Boolean>();
		for (WoofTemplateLinkModel link : this.linksSecure) {
			String linkName = link.getWoofTemplateLinkName();
			if (!(EclipseUtil.isBlank(linkName))) {
				Boolean isLinkSecure = Boolean.valueOf(link.getIsLinkSecure());
				results.put(linkName, isLinkSecure);
			}
		}
		return results;
	}

	/**
	 * Obtains the HTTP methods that are to trigger a redirect on rendering the
	 * {@link HttpTemplate}.
	 * 
	 * @return HTTP methods that are to trigger a redirect on rendering the
	 *         {@link HttpTemplate}.
	 */
	public String[] getRenderRedirectHttpMethods() {
		String[] httpMethods = null;
		String httpMethodsValue = this.renderRedirectHttpMethods.getValue();
		if (!(EclipseUtil.isBlank(httpMethodsValue))) {
			httpMethods = httpMethodsValue.split(",");
			for (int i = 0; i < httpMethods.length; i++) {
				httpMethods[i] = httpMethods[i].trim();
			}
		}
		return httpMethods;
	}

	/**
	 * Indicates if allow continue rendering.
	 * 
	 * @return <code>true</code> to allow continue rendering.
	 */
	public boolean isContinueRendering() {
		return Boolean.parseBoolean(this.isContinueRendering.getValue());
	}

	/**
	 * Obtains the {@link HttpTemplateExtensionInstance} listing.
	 * 
	 * @return {@link HttpTemplateExtensionInstance} listing.
	 */
	public HttpTemplateExtensionInstance[] getHttpTemplateExtensionInstances() {
		// TODO obtain the extension instances
		return null;
	}

	/*
	 * ====================== WizardPage =========================
	 */

	@Override
	public void createControl(Composite parent) {

		// Initiate the page
		final Composite page = new Composite(parent, SWT.NONE);
		page.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		page.setLayout(new GridLayout(2, false));

		// Obtain initial values
		String initialTemplatePath = "";
		String initialSuperTemplateName = "";
		if (this.templateInstance != null) {
			initialTemplatePath = getTextValue(this.templateInstance
					.getTemplatePath());
			initialSuperTemplateName = getTextValue(this.superTemplateName
					.getValue());
		}

		// Determine if valid src/main/webapp directory
		IFolder webappDirectory = this.project
				.getFolder(WoofOfficeFloorSource.WEBAPP_PATH);
		IFile webXmlFile = webappDirectory
				.getFile(WoofOfficeFloorSource.WEBXML_FILE_PATH);
		if ((webappDirectory != null && webappDirectory.exists())
				&& (!(webXmlFile.exists()))) {
			// Invalid webapp directory
			new Label(page, SWT.NONE);
			Label webappInvalid = new Label(page, SWT.NONE);
			webappInvalid.setForeground(ColorConstants.red);
			webappInvalid.setText("WARNING: "
					+ WoofOfficeFloorSource.WEBAPP_PATH
					+ " resources unavailable as missing "
					+ WoofOfficeFloorSource.WEBXML_FILE_PATH);
		}

		// Provide means to specify URI path
		SourceExtensionUtil.createPropertyText("URI path",
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, null, page,
				this, null);

		// Provide means to specify template location
		new Label(page, SWT.NONE).setText("Template path: ");
		this.templatePath = initialTemplatePath;
		InputHandler<String> path = new InputHandler<String>(page,
				new WoofFileInput(this.project, page.getShell()),
				this.templatePath, new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						// Specify the location and indicate changed
						HttpTemplateWizardPage.this.templatePath = (value == null ? ""
								: value.toString());
						HttpTemplateWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						HttpTemplateWizardPage.this.setErrorMessage(message);
					}
				});
		path.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Provide means to specify logic class
		SourceExtensionUtil
				.createPropertyClass("Logic class",
						HttpTemplateSectionSource.PROPERTY_CLASS_NAME, page,
						this, null);

		// Provide ability to specify super template
		String[] inheritableSuperTemplateNames = this.templateInheritances
				.keySet().toArray(new String[0]);
		Arrays.sort(inheritableSuperTemplateNames,
				String.CASE_INSENSITIVE_ORDER);
		SourceExtensionUtil.createPropertyCombo("Extend template",
				SUPER_TEMPLATE_NAME, initialSuperTemplateName,
				inheritableSuperTemplateNames, page, this, null);

		// Provide means to specify if secure
		SourceExtensionUtil.createPropertyCheckbox("Template secure",
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE, false,
				String.valueOf(true), String.valueOf(false), page, this, null);

		// Provide means to configure the links
		new Label(page, SWT.NONE).setText("Links secure: ");
		BeanListInput<WoofTemplateLinkModel> linksInput = new BeanListInput<WoofTemplateLinkModel>(
				WoofTemplateLinkModel.class, true);
		linksInput.addProperty("WoofTemplateLinkName", 3, "Link");
		linksInput.addProperty("IsLinkSecure", 1, "Secure");
		for (WoofTemplateLinkModel link : this.linksSecure) {
			linksInput.addBean(link);
		}
		InputHandler<WoofTemplateLinkModel[]> links = new InputHandler<WoofTemplateLinkModel[]>(
				page, linksInput, null, new InputListener() {
					@Override
					@SuppressWarnings("unchecked")
					public void notifyValueChanged(Object value) {
						// Specify the links and indicate changed
						List<WoofTemplateLinkModel> list = (List<WoofTemplateLinkModel>) value;
						HttpTemplateWizardPage.this.linksSecure = list
								.toArray(new WoofTemplateLinkModel[list.size()]);
						HttpTemplateWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						HttpTemplateWizardPage.this.setErrorMessage(message);
					}
				});
		links.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Provide means to specify render redirect HTTP methods
		SourceExtensionUtil
				.createPropertyText(
						"Render Redirect HTTP methods",
						HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
						null, page, this, null);

		// Provide means to specify if may continue rendering
		SourceExtensionUtil.createPropertyCheckbox("Continue Rendering",
				PROPERTY_IS_CONTINUE_RENDERING, false, String.valueOf(true),
				String.valueOf(false), page, this, null);

		// Configure the template extensions
		new Label(page, SWT.NONE).setText("Extensions");
		final TabFolder extensionTabs = new TabFolder(page, SWT.NONE);
		extensionTabs.setLayout(new GridLayout(1, false));
		extensionTabs
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Provide the extension add button
		final TabItem addExtensionTab = new TabItem(extensionTabs, SWT.NONE);
		addExtensionTab.setText("+");
		Label addExtensionLabel = new Label(extensionTabs, SWT.NONE);
		addExtensionLabel.setText("Press '+' to add an extension");
		addExtensionTab.setControl(addExtensionLabel);

		// Listen for click on add extension tab
		extensionTabs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				TabFolder curFolder = (TabFolder) e.widget;
				Point eventLocation = new Point(e.x, e.y);
				TabItem clickedTab = curFolder.getItem(eventLocation);
				if (clickedTab == addExtensionTab) {

					// Provide dialog to select extension
					HttpTemplateExtensionSourceInstance instance = SelectHttpTemplateExtensionSourceInstanceDialog.getHttpTemplateExtensionSourceInstance(
							extensionTabs.getShell(),
							HttpTemplateWizardPage.this.availableHttpTemplateExtensionSourceInstances);
					if (instance == null) {
						return; // not add extension
					}

					// Obtain the label of the extension
					String extensionLabel = instance
							.getWoofTemplateExtensionLabel();

					// Add a tab (as second last item)
					int tabIndex = Math.max(0,
							(extensionTabs.getItemCount() - 1));
					final TabItem extraTab = new TabItem(extensionTabs,
							SWT.NONE, tabIndex);
					extraTab.setText(extensionLabel);

					// Create the context for the extension
					PropertyList properties = instance
							.createSpecification(HttpTemplateWizardPage.this);
					WoofTemplateExtensionSourceExtensionContext context = new WoofTemplateExtensionSourceExtensionContextImpl(
							properties, HttpTemplateWizardPage.this);

					// Load the controls to configure the extension
					Composite panel = new Composite(extensionTabs, SWT.NONE);
					Composite controls = new Composite(panel, SWT.NONE);
					instance.createControl(controls, context);
					extraTab.setControl(panel);

					// Configure the layout (after to override)
					panel.setLayout(new GridLayout(1, false));
					panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));
					controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
							true, true));

					// Add extension to listing
					final HttpTemplateExtensionStruct templateExtension = new HttpTemplateExtensionStruct(
							instance, properties);
					HttpTemplateWizardPage.this.templateExtensions
							.add(templateExtension);

					// Add button to remove extension
					Button removeButton = new Button(panel, SWT.PUSH);
					removeButton.setText("Remove extension");
					removeButton.setLayoutData(new GridData(SWT.RIGHT,
							SWT.BOTTOM, false, false));
					removeButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {

							// Remove the template extension
							HttpTemplateWizardPage.this.templateExtensions
									.remove(templateExtension);

							// Remove from display
							extraTab.dispose();
							
							// Handle change of removing the extension
							HttpTemplateWizardPage.this.handleChange();
						}
					});

					// Provide focus to new tab
					extensionTabs.setSelection(tabIndex);

					// Handle change of adding the extension
					HttpTemplateWizardPage.this.handleChange();
				}
			}
		});

		// Indicate initial state
		this.handleChange();

		// Specify page control
		this.setControl(page);
	}

	/**
	 * Handles the change.
	 */
	private void handleChange() {

		// Clear error message (as change may have corrected it)
		this.setErrorMessage(null);

		// Clear section type (as potentially changing)
		this.sectionType = null;

		// Ensure have appropriate super template inheritance
		this.superTemplateInheritance = this.templateInheritances
				.get(this.superTemplateName.getValue());
		if (this.superTemplateInheritance == null) {
			// No inherited template paths
			this.inheritedTemplatePaths.setValue(null);
		} else {
			// Specify the inherited template paths
			this.inheritedTemplatePaths.setValue(this.superTemplateInheritance
					.getInheritedTemplatePathsPropertyValue());
		}

		// Ensure have template URI path
		if (EclipseUtil.isBlank(this.uriPath.getValue())) {
			this.setErrorMessage("Must specify URI path");
			this.setPageComplete(false);
			return;
		}

		// Ensure have template path
		if (EclipseUtil.isBlank(this.templatePath)) {
			this.setErrorMessage("Must specify location of template");
			this.setPageComplete(false);
			return;
		}

		// Load the Section Type
		this.isIssue = false; // reset to determine if issue
		this.sectionType = this.sectionLoader.loadSectionType(
				HttpTemplateSectionSource.class, this.templatePath,
				this.properties);
		if ((this.sectionType == null) || (this.isIssue)) {
			// Must have section (issue reported as error message)
			this.setPageComplete(false);
			return;
		}

		// Validate the template extensions
		for (HttpTemplateExtensionStruct extension : this.templateExtensions) {

			// Obtain the extension source class name
			String extensionSourceClassName = extension.source
					.getWoofTemplateExtensionSourceClassName();

			// Match against old extensions to find old properties
			PropertyList oldProperties = null;
			if (this.templateInstance != null) {
				for (HttpTemplateExtensionInstance instance : this.templateInstance
						.getTemplateExtensionInstances()) {
					if (extensionSourceClassName.equals(instance
							.getTemplateExtensionClassName())) {
						oldProperties = instance.getProperties();
					}
				}
			}

			// Obtain the remaining details
			String oldUri = this.initialUriPath;
			String newUri = this.uriPath.getValue();
			PropertyList newProperties = extension.properties;

			// Validate the change
			extension.source.validateChange(oldUri, oldProperties, newUri,
					newProperties, this.resourceSources, this);
			if (this.isIssue) {
				// Issue reported as error
				this.setPageComplete(false);
				return;
			}
		}

		// Specification of template details complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

	/*
	 * ===================== CompilerIssues ===============================
	 */

	/**
	 * Allows determining if an issue occurred. Reset this to <code>false</code>
	 * before undertaking operation to determine if causes an issue.
	 */
	private boolean isIssue = false;

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		// Provide as error message
		this.setErrorMessage(issueDescription);
		this.isIssue = true;
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {
		// Provide as error message
		this.setErrorMessage(issueDescription + " ("
				+ cause.getClass().getSimpleName() + ": " + cause.getMessage()
				+ ")");
		this.isIssue = true;
	}

	/*
	 * ================= SectionSourceExtensionContext ======================
	 */

	@Override
	public IProject getProject() {
		return this.project;
	}

	@Override
	public PropertyList getPropertyList() {
		return this.properties;
	}

	@Override
	public void notifyPropertiesChanged() {
		this.handleChange();
	}

	/**
	 * Details of the {@link WoofTemplateExtensionSource}.
	 */
	private static class HttpTemplateExtensionStruct {

		/**
		 * {@link HttpTemplateExtensionSourceInstance}.
		 */
		public final HttpTemplateExtensionSourceInstance source;

		/**
		 * {@link PropertyList}.
		 */
		public final PropertyList properties;

		/**
		 * Initiate.
		 * 
		 * @param source
		 *            {@link HttpTemplateExtensionSourceInstance}.
		 * @param properties
		 *            {@link PropertyList}.
		 */
		public HttpTemplateExtensionStruct(
				HttpTemplateExtensionSourceInstance source,
				PropertyList properties) {
			this.source = source;
			this.properties = properties;
		}
	}

}