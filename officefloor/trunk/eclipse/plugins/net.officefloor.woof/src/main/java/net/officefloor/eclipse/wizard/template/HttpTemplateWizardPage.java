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
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.util.EclipseUtil;
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
		CompilerIssues {

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link SectionLoader}.
	 */
	private final SectionLoader sectionLoader;

	/**
	 * {@link Text} of the {@link HttpTemplate} name.
	 */
	private Text templateName;

	/**
	 * Path to the {@link HttpTemplate}.
	 */
	private String templatePath;

	/**
	 * Name of the logic {@link Class}.
	 */
	private String logicClassName;

	/**
	 * URI for the {@link HttpTemplate}.
	 */
	private Text uri;

	/**
	 * {@link HttpTemplateInstance}.
	 */
	private HttpTemplateInstance instance = null;

	/**
	 * Initiate.
	 */
	protected HttpTemplateWizardPage(IProject project) {
		super("HTTP Template");
		this.project = project;

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the section loader
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler();
		this.compiler.setClassLoader(classLoader);
		this.compiler.setCompilerIssues(this);
		this.sectionLoader = this.compiler.getSectionLoader();

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

		// Obtain initial values
		String initialTemplateName = "";
		String initialTemplatePath = "";
		String initialLogicClassName = "";
		String initialUri = "";

		// Initiate the page
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		page.setLayout(new GridLayout(2, false));

		// Add means to specify HTTP Template Name
		new Label(page, SWT.NONE).setText("Template name: ");
		this.templateName = new Text(page, SWT.BORDER);
		this.templateName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		this.templateName.setText(initialTemplateName);
		this.templateName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				HttpTemplateWizardPage.this.handleChange();
			}
		});

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

		// Provide means to specify logic class
		new Label(page, SWT.NONE).setText("Logic class: ");
		this.templatePath = initialTemplatePath;
		InputHandler<String> logicClass = new InputHandler<String>(page,
				new ClasspathClassInput(this.project, initialLogicClassName,
						page.getShell()), new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						// Specify the logic class name
						HttpTemplateWizardPage.this.logicClassName = (value == null ? ""
								: value.toString());
						HttpTemplateWizardPage.this.handleChange();
					}

					@Override
					public void notifyValueInvalid(String message) {
						HttpTemplateWizardPage.this.setErrorMessage(message);
					}
				});
		logicClass.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

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
	private void handleChange() {

		// Clear instance (as changing)
		this.instance = null;

		// Ensure have template name
		String name = this.templateName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of template");
			this.setPageComplete(false);
			return;
		}

		// Ensure have template path
		if (EclipseUtil.isBlank(this.templatePath)) {
			this.setErrorMessage("Must specify location of template");
			this.setPageComplete(false);
			return;
		}

		// Ensure have logic class
		if (EclipseUtil.isBlank(this.logicClassName)) {
			this.setErrorMessage("Must specify logic class");
			this.setPageComplete(false);
			return;
		}

		// Load the OfficeSection
		PropertyList properties = this.compiler.createPropertyList();
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME)
				.setValue(this.logicClassName);
		OfficeSection section = this.sectionLoader.loadOfficeSection(name,
				HttpTemplateSectionSource.class, this.templatePath, properties);
		if (section == null) {
			// Must have section (issue reported as error message)
			this.setPageComplete(false);
			return;
		}

		// Obtain the URI
		String uriValue = this.uri.getText();
		uriValue = (EclipseUtil.isBlank(uriValue) ? null : uriValue.trim());

		// Create the HTTP Template Instance
		this.instance = new HttpTemplateInstance(name, this.templatePath,
				this.logicClassName, section, uriValue);

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

}