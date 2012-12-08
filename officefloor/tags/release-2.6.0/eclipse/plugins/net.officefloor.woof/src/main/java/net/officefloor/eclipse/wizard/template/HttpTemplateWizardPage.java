/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.util.LinkedList;
import java.util.List;

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
import net.officefloor.eclipse.common.dialog.input.impl.BooleanInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClassMethodInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.dialog.input.WoofFileInput;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.eclipse.web.HttpTemplateSectionSourceExtension;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
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
	 * Path to the {@link HttpTemplate}.
	 */
	private String templatePath;

	/**
	 * Logic class name.
	 */
	private String logicClassName;

	/**
	 * URI path for the {@link HttpTemplate}.
	 */
	private String uriPath;

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
		if (this.templateInstance != null) {
			// Add the properties from base instance
			this.properties.addProperty(
					HttpTemplateSectionSource.PROPERTY_CLASS_NAME).setValue(
					this.templateInstance.getLogicClassName());
			this.properties.addProperty(
					HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI).setValue(
					this.templateInstance.getUri());
		}

		// Specify the title
		this.setTitle("Add Template");
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
		return this.logicClassName;
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
	 * Obtains the URI path.
	 * 
	 * @return URI path.
	 */
	public String getUriPath() {
		return (EclipseUtil.isBlank(this.uriPath) ? null : this.uriPath.trim());
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
		String initialLogicClassName = "";
		// URI path already set in properties
		String initialGwtEntryPoint = "";
		String[] initialGwtAsyncInterfaces = new String[0];
		boolean initiallyEnableComet = false;
		String initialCometManualPublishMethodName = "";
		if (this.templateInstance != null) {
			initialTemplatePath = getTextValue(this.templateInstance
					.getTemplatePath());
			initialLogicClassName = getTextValue(this.templateInstance
					.getLogicClassName());
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

		// Provide means to configure the properties
		this.templateExtension.createControl(page, this);

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
		this.cometManualPublishMethodInput.setClassName(initialLogicClassName);
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

		// Ensure have template path
		if (EclipseUtil.isBlank(this.templatePath)) {
			this.setErrorMessage("Must specify location of template");
			this.setPageComplete(false);
			return;
		}

		// Obtain the possible logic class
		Property propertyLogicClass = this.properties
				.getProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME);
		String propertyLogicClassName = (propertyLogicClass == null ? null
				: propertyLogicClass.getValue());
		this.logicClassName = (EclipseUtil.isBlank(propertyLogicClassName) ? null
				: propertyLogicClassName);

		// Obtain the HTTP Template Section Source class
		Class sectionSourceClass = this.templateExtension
				.getSectionSourceClass();

		// Obtain the URI
		Property propertyUriPath = this.properties
				.getOrAddProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI);
		String propertyUriPathValue = propertyUriPath.getValue();
		this.uriPath = (EclipseUtil.isBlank(propertyUriPathValue) ? null
				: propertyUriPathValue);
		if (this.uriPath == null) {
			// URI path optional, so provide dummy value
			propertyUriPath.setValue("DUMMY");
		}

		// Load the Section Type
		this.sectionType = this.sectionLoader.loadSectionType(
				sectionSourceClass, this.templatePath, this.properties);
		if (this.sectionType == null) {
			// Must have section (issue reported as error message)
			this.setPageComplete(false);
			return;
		}

		// Clear URI path property if dummy value
		if (this.uriPath == null) {
			propertyUriPath.setValue(null);
		}

		// Ensure have URI if have GWT Entry Point Class
		String gwtEntryPoint = this.getGwtEntryPointClassName();
		if ((gwtEntryPoint != null) && (this.uriPath == null)) {
			// Must have URI if using GWT
			this.setErrorMessage("Must provide URI if using GWT");
			this.setPageComplete(false);
			return;
		}

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