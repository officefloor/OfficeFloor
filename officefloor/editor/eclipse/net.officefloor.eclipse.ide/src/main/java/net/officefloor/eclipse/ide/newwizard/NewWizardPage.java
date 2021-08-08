/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.eclipse.ide.newwizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Page to create a new {@link OfficeFloor} item.
 *
 * @author Daniel Sagenschneider
 */
class NewWizardPage extends WizardPage {

	/**
	 * {@link IStructuredSelection}.
	 */
	private final IStructuredSelection selection;

	/**
	 * Initial item name.
	 */
	private final String initialFileName;

	/**
	 * Extension of the item.
	 */
	private final String extension;

	/**
	 * {@link IContainer} for the new file.
	 */
	private Text containerText;

	/**
	 * Name of the new item.
	 */
	private Text itemText;

	/**
	 * Initiate.
	 *
	 * @param selection
	 *            {@link IStructuredSelection}.
	 * @param pageName
	 *            Name of the page.
	 * @param title
	 *            Title of the page.
	 * @param description
	 *            Description of the page.
	 * @param initialFileName
	 *            Initial file name.
	 * @param extension
	 *            Extension of the item.
	 */
	NewWizardPage(IStructuredSelection selection, String pageName, String title, String description,
			String initialFileName, String extension) {
		super(pageName);
		this.selection = selection;
		this.initialFileName = initialFileName;
		this.extension = extension;
		this.setTitle(title);
		this.setDescription(description);
	}

	/**
	 * Obtains the {@link IResource} container.
	 * 
	 * @return {@link IResource} for the container.
	 */
	public IResource getItemContainer() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(this.getItemContainerName()));
	}

	/**
	 * Obtains the {@link IContainer} name.
	 *
	 * @return {@link IContainer} name.
	 */
	public String getItemContainerName() {
		return this.containerText.getText();
	}

	/**
	 * Obtain the full item name including the extension.
	 *
	 * @return Full item name.
	 */
	public String getItemFullName() {
		String itemName = this.getItemName();
		if (!itemName.trim().endsWith("." + this.extension)) {
			itemName += "." + this.extension;
		}
		return itemName;
	}

	/**
	 * Obtains the item name.
	 *
	 * @return Item name.
	 */
	public String getItemName() {
		return this.itemText.getText();
	}

	/*
	 * ====================== IDialogPage ====================================
	 */

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));

		// Container
		new Label(container, SWT.NULL).setText("Container:");
		this.containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		this.containerText.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		this.containerText.addListener(SWT.Modify, (event) -> NewWizardPage.this.handleDialogChanged());
		this.containerText.addListener(SWT.Verify, (event) -> NewWizardPage.this.handleDialogChanged());
		Button containerBrowse = new Button(container, SWT.PUSH);
		containerBrowse.setText("Browse...");
		containerBrowse.addListener(SWT.Selection, (event) -> NewWizardPage.this.handleContainerBrowse());

		// Item name
		new Label(container, SWT.NULL).setText("Name:");
		this.itemText = new Text(container, SWT.BORDER | SWT.SINGLE);
		this.itemText.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		this.itemText.addListener(SWT.Modify, (event) -> NewWizardPage.this.handleDialogChanged());
		this.itemText.addListener(SWT.Verify, (event) -> NewWizardPage.this.handleDialogChanged());
		this.itemText.addListener(SWT.Selection, (event) -> NewWizardPage.this.itemText.selectAll());
		this.itemText.addListener(SWT.MouseDown, (event) -> NewWizardPage.this.itemText.selectAll());

		// Initialise page
		this.initialise();
		this.handleDialogChanged();
		this.setControl(container);
	}

	/**
	 * Initialise.
	 */
	private void initialise() {

		// Specify the container
		if ((this.selection != null) && (!this.selection.isEmpty()) && (this.selection.size() == 1)) {
			Object object = this.selection.getFirstElement();

			// Obtain the resource
			IResource resource;
			if (object instanceof IResource) {
				resource = (IResource) object;
			} else if (object instanceof IJavaElement) {
				resource = ((IJavaElement) object).getResource();
			} else {
				// Unknown resource
				resource = null;
			}

			// Obtain the container
			IContainer container;
			if (resource == null) {
				container = null;
			} else if (resource instanceof IContainer) {
				container = (IContainer) resource;
			} else {
				container = resource.getParent();
			}

			// Specify the container path
			String containerPath = (container == null ? "" : container.getFullPath().toString());
			this.containerText.setText(containerPath);
		}

		// Specify the initial item name
		this.itemText.setText(this.initialFileName);
	}

	/**
	 * Handle browsing for a container.
	 */
	protected void handleContainerBrowse() {
		// Dialog to select container
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(this.getShell(),
				ResourcesPlugin.getWorkspace().getRoot(), false, "Select new container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			// Specify container
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				this.containerText.setText(result[0].toString());
			}
		}
	}

	/**
	 * Handles change of the dialog.
	 */
	private void handleDialogChanged() {

		// Ensure container is valid
		if (this.getItemContainerName().trim().length() == 0) {
			this.updateStatus("Container must be specified");
			return;
		}
		IResource container = this.getItemContainer();
		if ((container == null) || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			this.updateStatus("Container must exist");
			return;
		}
		if (!container.isAccessible()) {
			this.updateStatus("Project must be writable");
			return;
		}

		// Ensure item is valid
		String itemName = this.getItemName().trim();
		if (itemName.length() == 0) {
			this.updateStatus("Name must be specified");
			return;
		}
		if (itemName.replace('\\', '/').indexOf('/', 1) > 0) {
			this.updateStatus("Name must be valid");
			return;
		}
		if (itemName.contains(".")) {
			if (!itemName.endsWith("." + this.extension)) {
				this.updateStatus("Extension must be '" + this.extension + "'");
				return;
			}
		}

		// May complete
		updateStatus(null);
	}

	/**
	 * Updates the status of this dialog.
	 *
	 * @param message
	 *            Message indicating status. <code>null</code> means complete.
	 */
	private void updateStatus(String message) {
		this.setErrorMessage(message);
		this.setPageComplete(message == null);
	}

}
