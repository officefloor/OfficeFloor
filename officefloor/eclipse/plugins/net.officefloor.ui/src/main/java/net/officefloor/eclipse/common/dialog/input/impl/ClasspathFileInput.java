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
package net.officefloor.eclipse.common.dialog.input.impl;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.layout.NoMarginGridLayout;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.LogUtil;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

/**
 * {@link Input} to obtain a file from the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathFileInput implements Input<Composite> {

	/**
	 * {@link IContainer} to find the file within.
	 */
	private final IContainer container;

	/**
	 * {@link Shell}.
	 */
	private final Shell shell;

	/**
	 * {@link Text} containing the file name.
	 */
	private Text fileName;

	/**
	 * Initiate.
	 * 
	 * @param container
	 *            {@link IContainer} to find the file within.
	 * @param shell
	 *            {@link Shell}.
	 */
	public ClasspathFileInput(IContainer container, Shell shell) {
		this.container = container;
		this.shell = shell;
	}

	/**
	 * Convenience constructor to use the {@link IProject} and {@link Shell} of
	 * the {@link IEditorPart}.
	 * 
	 * @param editor
	 *            {@link IEditorPart}.
	 */
	public ClasspathFileInput(IEditorPart editor) {
		this(ProjectConfigurationContext.getProject(editor.getEditorInput()),
				editor.getEditorSite().getShell());
	}

	/**
	 * <p>
	 * Transforms the {@link IFile} to the path to use.
	 * <p>
	 * Allows overriding for more specialised transformation.
	 * 
	 * @param file
	 *            {@link IFile}.
	 * @return path to use.
	 */
	protected String transformToPath(IFile file) {
		return ClasspathUtil.getClassPathLocation(file.getFullPath());
	}

	/*
	 * ================= Input ===================================
	 */

	@Override
	public Composite buildControl(final InputContext context) {

		// Create composite to contain text and completion button
		final Composite container = new Composite(context.getParent(), SWT.NONE);
		container.setLayout(NoMarginGridLayout.create(2, false));

		// Create text box to contain file name
		this.fileName = new Text(container, SWT.BORDER);
		this.fileName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.fileName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Notify of change
				String inputText = ClasspathFileInput.this.fileName.getText();
				context.notifyValueChanged(inputText);
			}
		});

		// Provide auto completion on file
		KeyStroke keyStroke = KeyStroke.getInstance(SWT.CTRL, ' ');
		ContentProposalAdapter autoCompletion = new ContentProposalAdapter(
				this.fileName, new TextContentAdapter(),
				new JavaElementContentProposalProvider(), keyStroke,
				new char[0]);
		autoCompletion
				.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		// Add button to search for file
		Button button = new Button(container, SWT.PUSH);
		button.setText("...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Select the file
				ClasspathFileInput.this
						.doFileSelection(ClasspathFileInput.this.fileName
								.getText());
			}
		});

		// Specify the initial file name
		Object initialValue = context.getInitialValue();
		String initialFileName = (initialValue == null ? "" : initialValue
				.toString());
		this.fileName.setText(initialFileName);

		// Return the container
		return container;
	}

	@Override
	public Object getValue(Composite control, InputContext context) {
		return this.fileName.getText();
	}

	/**
	 * {@link IJavaElement} {@link IContentProposalProvider}.
	 */
	private class JavaElementContentProposalProvider implements
			IContentProposalProvider {

		/*
		 * ================== IContentProposalProvider =====================
		 */

		@Override
		public IContentProposal[] getProposals(String contents, int position) {

			// Obtain the contents for auto completion
			contents = contents.substring(0, position);
			contents = (contents == null ? "" : contents);

			// Select the class
			ClasspathFileInput.this.doFileSelection(contents);

			// Class selected (or cancel), no auto complete options required
			return new IContentProposal[0];
		}
	}

	/**
	 * Does the selection of a file.
	 * 
	 * @param filter
	 *            Filter.
	 */
	private void doFileSelection(String filter) {
		try {

			// Strip filter down to just the simple name
			int index = filter.lastIndexOf('/');
			if (index >= 0) {
				filter = filter.substring(index + "/".length());
			}
			index = filter.indexOf('.');
			if (index >= 0) {
				filter = filter.substring(0, index);
			}

			// Obtain the selected file
			FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
					this.shell, false, this.container, IResource.FILE);
			dialog.setInitialPattern(filter);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] results = dialog.getResult();
			if ((results == null) || (results.length != 1)) {
				return; // cancel
			}

			// Obtain the selected item
			Object selectedItem = results[0];
			if (selectedItem instanceof IFile) {
				// Specify class path location for file
				IFile file = (IFile) selectedItem;
				String filePath = this.transformToPath(file);
				this.fileName.setText(filePath);
			} else {
				// Unknown type
				LogUtil.logError("Unknown type: "
						+ selectedItem.getClass().getName());
			}

		} catch (Exception ex) {
			LogUtil.logError("Failed obtain file", ex);
		}
	}
}