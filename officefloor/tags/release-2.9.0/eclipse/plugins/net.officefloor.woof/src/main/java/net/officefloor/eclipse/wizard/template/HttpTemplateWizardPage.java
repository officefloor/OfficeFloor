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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.csv.InputFactory;
import net.officefloor.eclipse.common.dialog.input.csv.ListInput;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.common.dialog.input.impl.BooleanInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClassMethodInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.dialog.input.WoofFileInput;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.eclipse.web.HttpTemplateSectionSourceExtension;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.model.woof.WoofTemplateLinkModel;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.plugin.woof.WoofContextConfigurable;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page providing the details of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWizardPage extends WizardPage implements
		CompilerIssues, SectionSourceExtensionContext {

	/**
	 * {@link Property} indicating whether may continue rendering.
	 */
	private static final String PROPERTY_IS_CONTINUE_RENDERING = "is.continue.rendering";

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
	 * Indicates if the template is secure.
	 */
	private final Property isTemplateSecure;

	/**
	 * Listing of {@link WoofTemplateLinkModel} instances.
	 */
	private WoofTemplateLinkModel[] linksSecure = new WoofTemplateLinkModel[0];

	/**
	 * HTTP methods that trigger a redirect on rendeirng the
	 * {@link HttpTemplate}.
	 */
	private final Property renderRedirectHttpMethods;

	/**
	 * Indicates whether allow continue rendering.
	 */
	private final Property isContinueRendering;

	/**
	 * GWT EntryPoint class name.
	 */
	private String gwtEntryPointClassName;

	/**
	 * GWT Service Async Interfaces.
	 */
	private String[] gwtServiceAsyncInterfaces;

	/**
	 * {@link InputHandler} to enable Comet.
	 */
	private InputHandler<Boolean> enableComet;

	/**
	 * {@link ClassMethodInput} for the Comet Manual Publish {@link Method}
	 * name.
	 */
	private ClassMethodInput cometManualPublishMethodInput;

	/**
	 * {@link InputHandler} for the Comet Manual Publish {@link Method} name.
	 */
	private InputHandler<String> cometManualPublishMethodName;

	/**
	 * {@link SectionType} for the {@link HttpTemplateInstance}.
	 */
	private SectionType sectionType = null;

	/**
	 * {@link HttpTemplateSectionSourceExtension}.
	 */
	private HttpTemplateSectionSourceExtension templateExtension;

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param templateInstance
	 *            {@link HttpTemplateInstance}.
	 */
	protected HttpTemplateWizardPage(final IProject project,
			final AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			HttpTemplateInstance templateInstance) {
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
			}
		};

		// Load access to web resources
		File projectDir = project.getLocation().toFile();
		WoofOfficeFloorSource.loadWebResourcesFromMavenProject(configurable,
				projectDir);

		// Obtain the section loader
		this.sectionLoader = this.compiler.getSectionLoader();

		// Create the property list (and load existing properties)
		this.properties = this.compiler.createPropertyList();
		String initialUriPath = null;
		String initialLogicClassName = null;
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

		// Add the properties
		this.uriPath = this
				.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI,
						initialUriPath);
		this.logicClassName = this.addProperty(
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				initialLogicClassName);
		this.isTemplateSecure = this.addProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				initialIsTemplateSecure);
		this.renderRedirectHttpMethods = this
				.addProperty(
						HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
						initialRenderRedirectHttpMethods);
		this.isContinueRendering = this.addProperty(
				PROPERTY_IS_CONTINUE_RENDERING, initialIsContinueRendering);

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
	 * Obtains the GWT EntryPoint class name.
	 * 
	 * @return GWT EntryPoint class name.
	 */
	public String getGwtEntryPointClassName() {
		return (EclipseUtil.isBlank(this.gwtEntryPointClassName) ? null
				: this.gwtEntryPointClassName);
	}

	/**
	 * Obtains the GWT Async Interface names.
	 * 
	 * @return GWT Async Interface names.
	 */
	public String[] getGwtAsyncInterfaceNames() {

		// Obtain the listing of async interfaces
		List<String> gwtAsyncList = new LinkedList<String>();
		if (this.gwtServiceAsyncInterfaces != null) {
			for (Object asyncInterface : this.gwtServiceAsyncInterfaces) {
				String asyncInterfaceName = (asyncInterface == null ? null
						: asyncInterface.toString());
				if (!(EclipseUtil.isBlank(asyncInterfaceName))) {
					gwtAsyncList.add(asyncInterfaceName);
				}
			}
		}
		String[] gwtAsyncInterfaces = gwtAsyncList
				.toArray(new String[gwtAsyncList.size()]);

		// Return the async interface listing
		return gwtAsyncInterfaces;
	}

	/**
	 * Indicates if Comet is enabled.
	 * 
	 * @return <code>true</code> if Comet is enabled.
	 */
	public boolean isEnableComet() {
		return this.enableComet.getTrySafeValue().booleanValue();
	}

	/**
	 * Obtains the Comet manual publish method name.
	 * 
	 * @return Comet manual publish method name.
	 */
	public String getCometManualPublishMethodName() {
		return this.cometManualPublishMethodName.getTrySafeValue();
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

		// Obtain HTTP Template Section Source Extension
		this.templateExtension = (HttpTemplateSectionSourceExtension) ExtensionUtil
				.createSectionSourceExtensionMap().get(
						HttpTemplateSectionSource.class.getName());
		if (this.templateExtension == null) {
			// Provide error that not able to obtain extension
			new Label(page, SWT.NONE)
					.setText("FATAL ERROR: unable to obtain plug-in extension "
							+ HttpTemplateSectionSourceExtension.class
									.getName());
			this.handleChange();
			return;
		}

		// Obtain initial values
		String initialTemplatePath = "";
		String initialGwtEntryPoint = "";
		String[] initialGwtAsyncInterfaces = new String[0];
		boolean initiallyEnableComet = false;
		String initialCometManualPublishMethodName = "";
		if (this.templateInstance != null) {
			initialTemplatePath = getTextValue(this.templateInstance
					.getTemplatePath());
			initialGwtEntryPoint = getTextValue(this.templateInstance
					.getGwtEntryPointClassName());
			initialGwtAsyncInterfaces = this.templateInstance
					.getGwtServerAsyncInterfaceNames();
			initiallyEnableComet = this.templateInstance.isEnableComet();
			initialCometManualPublishMethodName = getTextValue(this.templateInstance
					.getCometManualPublishMethodName());
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

		// Provide means to specify URI path
		SourceExtensionUtil
				.createPropertyText(
						"Render Redirect HTTP methods",
						HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
						null, page, this, null);

		// Provide means to specify if may continue rendering
		SourceExtensionUtil.createPropertyCheckbox("Continue Rendering",
				PROPERTY_IS_CONTINUE_RENDERING, false, String.valueOf(true),
				String.valueOf(false), page, this, null);

		// Provide means to specify GWT extension
		new Label(page, SWT.NONE).setText("GWT Enty Point Class: ");
		this.gwtEntryPointClassName = initialGwtEntryPoint;
		InputHandler<String> gwtEntryPoint = new InputHandler<String>(page,
				new ClasspathClassInput(this.project, page.getShell()),
				this.gwtEntryPointClassName, new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						// Specify the EntyPoint and indicate changed
						HttpTemplateWizardPage.this.gwtEntryPointClassName = (value == null ? ""
								: value.toString());
						HttpTemplateWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						HttpTemplateWizardPage.this.setErrorMessage(message);
					}
				});
		gwtEntryPoint.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Provide means to specify GWT Service Async Interfaces
		new Label(page, SWT.NONE).setText("GWT Service Async Interfaces: ");
		ListInput<Composite> gwtServicesAsyncInterfacesListInput = new ListInput<Composite>(
				String.class, page, new InputFactory<Composite>() {
					@Override
					public Input<Composite> createInput() {
						return new ClasspathClassInput(
								HttpTemplateWizardPage.this.project,
								page.getShell());
					}
				});
		this.gwtServiceAsyncInterfaces = initialGwtAsyncInterfaces;
		InputHandler<String[]> gwtAsyncServices = new InputHandler<String[]>(
				page, gwtServicesAsyncInterfacesListInput,
				this.gwtServiceAsyncInterfaces, new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						// Specify GWT Async Interfaces and indicate changed
						HttpTemplateWizardPage.this.gwtServiceAsyncInterfaces = (String[]) value;
						HttpTemplateWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						HttpTemplateWizardPage.this.setErrorMessage(message);
					}
				});
		gwtAsyncServices.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Provide means to enable Comet
		new Label(page, SWT.NONE).setText("Enable Comet: ");
		this.enableComet = new InputHandler<Boolean>(page, new BooleanInput(),
				Boolean.valueOf(initiallyEnableComet), new InputListener() {
					@Override
					public void notifyValueInvalid(String message) {
						// Should never be invalid
					}

					@Override
					public void notifyValueChanged(Object value) {
						// Handle change
						HttpTemplateWizardPage.this.handleChange();
					}
				});

		// Provide means to specify manual publish handle method name
		new Label(page, SWT.NONE).setText("Comet Manual Publish Method: ");
		this.cometManualPublishMethodInput = new ClassMethodInput(
				this.classLoader);
		this.cometManualPublishMethodInput.setClassName(this.logicClassName
				.getValue());
		this.cometManualPublishMethodName = new InputHandler<String>(page,
				this.cometManualPublishMethodInput,
				initialCometManualPublishMethodName, new InputListener() {
					@Override
					public void notifyValueInvalid(String message) {
						// Should never be invalid
					}

					@Override
					public void notifyValueChanged(Object value) {
						// Handle change
						HttpTemplateWizardPage.this.handleChange();
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleChange() {

		// Clear error message (as change may have corrected it)
		this.setErrorMessage(null);

		// Enable/Disable Comet Method Name based on whether using Comet
		Boolean isEnabled = this.enableComet.getTrySafeValue();
		this.cometManualPublishMethodName.getControl().setEnabled(
				isEnabled.booleanValue());

		// Ensure have template extension
		if (this.templateExtension == null) {
			this.setErrorMessage("FATAL ERROR: unable to source extension "
					+ HttpTemplateSectionSourceExtension.class.getName());
			this.setPageComplete(false);
			return;
		}

		// Clear section type (as potentially changing)
		this.sectionType = null;

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

		// Obtain the HTTP Template Section Source class
		Class sectionSourceClass = this.templateExtension
				.getSectionSourceClass();

		// Load the Section Type
		this.sectionType = this.sectionLoader.loadSectionType(
				sectionSourceClass, this.templatePath, this.properties);
		if (this.sectionType == null) {
			// Must have section (issue reported as error message)
			this.setPageComplete(false);
			return;
		}

		// Obtain the GWT Entry Point Class
		String gwtEntryPoint = this.getGwtEntryPointClassName();

		// Obtain the GWT Service Async Interfaces
		String[] gwtAsyncInterfaces = this.getGwtAsyncInterfaceNames();

		// Ensure have GWT Entry Point Class if have GWT Services
		if ((gwtAsyncInterfaces.length > 0) && (gwtEntryPoint == null)) {
			// Must have GWT Entry Point Class
			this.setErrorMessage("Must GWT Entry Point if using GWT Services");
			this.setPageComplete(false);
			return;
		}

		// Specification of template details complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

	/*
	 * ===================== CompilerIssues ===============================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		// Provide as error message
		this.setErrorMessage(issueDescription);
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {
		// Provide as error message
		this.setErrorMessage(issueDescription + " ("
				+ cause.getClass().getSimpleName() + ": " + cause.getMessage()
				+ ")");
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

		// Notify potential change to Template Logic class name
		String templateLogicClassName = this.properties.getPropertyValue(
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME, null);
		this.cometManualPublishMethodInput.setClassName(templateLogicClassName);
	}

}