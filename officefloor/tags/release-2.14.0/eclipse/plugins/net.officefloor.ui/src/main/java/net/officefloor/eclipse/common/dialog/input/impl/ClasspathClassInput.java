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

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.layout.NoMarginGridLayout;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.LogUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * {@link Input} to obtain an item from the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathClassInput implements Input<Composite> {

	/**
	 * {@link IJavaProject} to restrict classes.
	 */
	private final IJavaProject project;

	/**
	 * {@link Shell}.
	 */
	private final Shell shell;

	/**
	 * {@link Text} containing the class name.
	 */
	private Text className;

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject} to be root.
	 * @param shell
	 *            {@link Shell}.
	 */
	public ClasspathClassInput(IProject project, Shell shell) {
		this.project = JavaCore.create(project);
		this.shell = shell;
	}

	/**
	 * Convenience constructor to use the {@link IProject} and {@link Shell} of
	 * the {@link IEditorInput}.
	 * 
	 * @param editor
	 *            {@link IEditorInput}.
	 * @param shell
	 *            {@link Shell}.
	 */
	public ClasspathClassInput(IEditorPart editor) {
		this(ProjectConfigurationContext.getProject(editor.getEditorInput()),
				editor.getEditorSite().getShell());
	}

	/*
	 * ================= Input ===================================
	 */

	@Override
	public Composite buildControl(final InputContext context) {

		// Create composite to contain file expression and selection tree
		final Composite container = new Composite(context.getParent(), SWT.NONE);
		container.setLayout(NoMarginGridLayout.create(2, false));

		// Create text box to contain value
		this.className = new Text(container, SWT.BORDER);
		this.className.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		this.className.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Notify of change
				String inputText = ClasspathClassInput.this.className.getText();
				context.notifyValueChanged(inputText);
			}
		});

		// Provide auto completion on classes
		KeyStroke keyStroke = KeyStroke.getInstance(SWT.CTRL, ' ');
		ContentProposalAdapter autoCompletion = new ContentProposalAdapter(
				this.className, new TextContentAdapter(),
				new JavaElementContentProposalProvider(), keyStroke,
				new char[0]);
		autoCompletion
				.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		// Add button to search for class
		Button button = new Button(container, SWT.PUSH);
		button.setText("...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Select the class
				ClasspathClassInput.this
						.doClassSelection(ClasspathClassInput.this.className
								.getText());
			}
		});

		// Specify the initial class name
		Object initialValue = context.getInitialValue();
		String initialClassName = (initialValue == null ? "" : initialValue
				.toString());
		this.className.setText(initialClassName);

		// Return the container
		return container;
	}

	@Override
	public Object getValue(Composite control, InputContext context) {
		return this.className.getText();
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
			ClasspathClassInput.this.doClassSelection(contents);

			// Class selected (or cancel), no auto complete options required
			return new IContentProposal[0];
		}
	}

	/**
	 * Does the selection of a class.
	 * 
	 * @param filter
	 *            Filter.
	 */
	private void doClassSelection(String filter) {
		try {
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
					new IJavaProject[] { ClasspathClassInput.this.project },
					true);
			SelectionDialog dialog = JavaUI
					.createTypeDialog(
							ClasspathClassInput.this.shell,
							new ProgressMonitorDialog(
									ClasspathClassInput.this.shell),
							scope,
							IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
							false, filter);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] results = dialog.getResult();
			if ((results == null) || (results.length != 1)) {
				return; // cancel
			}

			// Obtain the selected item
			Object selectedItem = results[0];
			if (selectedItem instanceof IType) {
				// Set text to the type
				String text = ((IType) selectedItem).getFullyQualifiedName();
				ClasspathClassInput.this.className.setText(text);
			} else {
				// Unknown type
				LogUtil.logError("Unknown type: "
						+ selectedItem.getClass().getName());
			}

		} catch (Exception ex) {
			LogUtil.logError("Failed obtain type", ex);
		}
	}
}