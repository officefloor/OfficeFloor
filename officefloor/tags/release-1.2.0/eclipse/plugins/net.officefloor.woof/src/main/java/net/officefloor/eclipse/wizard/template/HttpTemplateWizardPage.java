/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.eclipse.web.HttpTemplateSectionSourceExtension;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page providing the details of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWizardPage extends WizardPage implements
		CompilerIssues, SectionSourceExtensionContext {

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
	 * URI for the {@link HttpTemplate}.
	 */
	private Text uri;

	/**
	 * {@link HttpTemplateInstance}.
	 */
	private HttpTemplateInstance instance = null;

	/**
	 * {@link HttpTemplateSectionSourceExtension}.
	 */
	private HttpTemplateSectionSourceExtension templateExtension;

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 */
	protected HttpTemplateWizardPage(IProject project) {
		super("HTTP Template");
		this.project = project;

		// Obtain the class loader for the project
		ClassLoader parent = WoofPlugin.getDefault().getClass()
				.getClassLoader();
		this.classLoader = ProjectClassLoader.create(project, parent);

		// Obtain the section loader
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler();
		this.compiler.setClassLoader(this.classLoader);
		this.compiler.setCompilerIssues(this);
		this.sectionLoader = this.compiler.getSectionLoader();

		// Create the property list
		this.properties = this.compiler.createPropertyList();

		// Specify the title
		this.setTitle("Add Template");
	}

	/**
	 * Obtains the {@link HttpTemplateInstance}.
	 * 
	 * @return {@link HttpTemplateInstance}.
	 */
	public HttpTemplateInstance getHttpTemplateInstance() {
		return this.instance;
	}

	/*
	 * ====================== WizardPage =========================
	 */

	@Override
	public void createControl(Composite parent) {

		// Initiate the page
		Composite page = new Composite(parent, SWT.NONE);
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
		String initialUri = "";

		// Provide means to specify template location
		new Label(page, SWT.NONE).setText("Template path: ");
		this.templatePath = initialTemplatePath;
		InputHandler<String> path = new InputHandler<String>(page,
				new ClasspathFileInput(this.project, initialTemplatePath,
						page.getShell()), new InputListener() {
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

		// Add means to specify URI
		new Label(page, SWT.NONE).setText("URI: ");
		this.uri = new Text(page, SWT.BORDER);
		this.uri.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.uri.setText(initialUri);
		this.uri.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the uri changed
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

		// Ensure have template extension
		if (this.templateExtension == null) {
			this.setErrorMessage("FATAL ERROR: unable to source extension "
					+ HttpTemplateSectionSourceExtension.class.getName());
			this.setPageComplete(false);
			return;
		}

		// Clear instance (as changing)
		this.instance = null;

		// Ensure have template path
		if (EclipseUtil.isBlank(this.templatePath)) {
			this.setErrorMessage("Must specify location of template");
			this.setPageComplete(false);
			return;
		}

		// Ensure have logic class
		Property propertyLogicClass = this.properties
				.getProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME);
		if ((propertyLogicClass == null)
				|| (EclipseUtil.isBlank(propertyLogicClass.getValue()))) {
			this.setErrorMessage("Must specify logic class");
			this.setPageComplete(false);
			return;
		}

		// Obtain the HTTP Template Section Source class
		Class sectionSourceClass = this.templateExtension
				.getSectionSourceClass();

		// Load the Section Type
		SectionType sectionType = this.sectionLoader.loadSectionType(
				sectionSourceClass, this.templatePath, this.properties);
		if (sectionType == null) {
			// Must have section (issue reported as error message)
			this.setPageComplete(false);
			return;
		}

		// Obtain the URI
		String uriValue = this.uri.getText();
		uriValue = (EclipseUtil.isBlank(uriValue) ? null : uriValue.trim());

		// Create the HTTP Template Instance
		String logicClassName = propertyLogicClass.getValue();
		this.instance = new HttpTemplateInstance(this.templatePath,
				logicClassName, sectionType, uriValue);

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
	}

}